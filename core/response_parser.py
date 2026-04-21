"""
core/response_parser.py — Extracts structured diagnosis data from the model's
raw text output.

The model is instructed to respond in JSON.  This parser handles:
  • Clean JSON responses
  • JSON embedded in markdown fences
  • Graceful fallback via regex when JSON is malformed
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from typing import Optional

from utils.config import CONFIDENCE_HIGH, CONFIDENCE_LOW
from utils.helpers import get_logger, clamp

logger = get_logger(__name__)


# ── Data model ────────────────────────────────────────────────────────────────

@dataclass
class DiagnosisResult:
    diagnosis: str               = "Unknown"
    confidence: float            = 0.0        # 0.0 – 1.0
    reasoning: str               = ""
    next_steps: list[str]        = field(default_factory=list)
    urgency: str                 = "unknown"  # low | medium | high
    raw_response: str            = ""
    parse_method: str            = "none"     # json | regex | fallback

    @property
    def confidence_label(self) -> str:
        if self.confidence >= CONFIDENCE_HIGH:
            return "high"
        if self.confidence >= CONFIDENCE_LOW:
            return "medium"
        return "low"

    def to_dict(self) -> dict:
        return {
            "diagnosis":        self.diagnosis,
            "confidence":       round(self.confidence, 3),
            "confidence_label": self.confidence_label,
            "reasoning":        self.reasoning,
            "next_steps":       self.next_steps,
            "urgency":          self.urgency,
            "parse_method":     self.parse_method,
        }


# ── Parser ────────────────────────────────────────────────────────────────────

class ResponseParser:
    """Parses raw model output into a DiagnosisResult."""

    # Keys the model is expected to use (with common aliases)
    _DIAGNOSIS_KEYS   = ("diagnosis", "condition", "disease", "impression")
    _CONFIDENCE_KEYS  = ("confidence", "confidence_pct", "score", "probability")
    _REASONING_KEYS   = ("reasoning", "rationale", "explanation", "analysis")
    _NEXT_STEPS_KEYS  = ("next_steps", "recommendations", "advice", "actions")
    _URGENCY_KEYS     = ("urgency", "urgency_level", "priority", "severity")

    def parse(self, raw: str) -> DiagnosisResult:
        result = DiagnosisResult(raw_response=raw)

        # 1. Try clean / fenced JSON
        data = self._try_json(raw)
        if data:
            self._fill_from_dict(result, data)
            result.parse_method = "json"
            logger.debug("Parsed via JSON.")
            return result

        # 2. Regex fallback
        self._fill_from_regex(result, raw)
        result.parse_method = "regex"
        logger.warning("JSON parse failed — using regex fallback.")
        return result

    # ── JSON parsing ──────────────────────────────────────────────────────────

    @staticmethod
    def _try_json(text: str) -> Optional[dict]:
        # Strip markdown code fences if present
        fenced = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", text, re.S)
        candidate = fenced.group(1) if fenced else text.strip()

        # Try to extract bare JSON object
        obj_match = re.search(r"\{.*\}", candidate, re.S)
        if obj_match:
            candidate = obj_match.group(0)

        try:
            return json.loads(candidate)
        except json.JSONDecodeError:
            return None

    def _fill_from_dict(self, result: DiagnosisResult, data: dict):
        result.diagnosis  = self._first(data, self._DIAGNOSIS_KEYS, "Unknown")
        result.reasoning  = self._first(data, self._REASONING_KEYS, "")
        result.urgency    = str(self._first(data, self._URGENCY_KEYS, "unknown")).lower()

        raw_conf = self._first(data, self._CONFIDENCE_KEYS, 0)
        result.confidence = self._normalise_confidence(raw_conf)

        steps = self._first(data, self._NEXT_STEPS_KEYS, [])
        if isinstance(steps, str):
            steps = [s.strip() for s in re.split(r"[;,\n]", steps) if s.strip()]
        result.next_steps = steps

    # ── Regex fallback ────────────────────────────────────────────────────────

    def _fill_from_regex(self, result: DiagnosisResult, text: str):
        # Diagnosis
        m = re.search(
            r"(?:diagnosis|condition|disease|impression)[:\s]+([^\n.]+)", text, re.I
        )
        if m:
            result.diagnosis = m.group(1).strip()

        # Confidence  (accept "75%", "0.75", "75 percent")
        m = re.search(
            r"(?:confidence|score|probability)[:\s]+([\d.]+)\s*%?", text, re.I
        )
        if m:
            result.confidence = self._normalise_confidence(float(m.group(1)))

        # Urgency
        m = re.search(r"\b(low|medium|high)\b.*?urgency", text, re.I)
        if not m:
            m = re.search(r"urgency[:\s]+(low|medium|high)", text, re.I)
        if m:
            result.urgency = m.group(1).lower()

        # Reasoning  (first substantive sentence after "reason" keyword)
        m = re.search(
            r"(?:reasoning|rationale|because)[:\s]+([^\n]+)", text, re.I
        )
        if m:
            result.reasoning = m.group(1).strip()

        # Next steps — numbered list lines
        steps = re.findall(r"(?:^|\n)\s*\d+[.)]\s*(.+)", text)
        if steps:
            result.next_steps = [s.strip() for s in steps]

    # ── Utilities ─────────────────────────────────────────────────────────────

    @staticmethod
    def _first(data: dict, keys: tuple, default):
        for k in keys:
            if k in data:
                return data[k]
        return default

    @staticmethod
    def _normalise_confidence(raw) -> float:
        """Convert % integer (75) or fraction (0.75) to a 0–1 float."""
        try:
            val = float(raw)
        except (TypeError, ValueError):
            return 0.0
        if val > 1.0:
            val = val / 100.0
        return clamp(val)