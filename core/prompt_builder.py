"""
core/prompt_builder.py — Assembles the full prompt sent to Qwen2-VL.

Prompt structure
----------------
<system>   system_prompt.txt
<examples> few_shots.json  (injected as alternating user/assistant turns)
<user>     symptom context + optional image tokens
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Optional

from utils.config import SYSTEM_PROMPT_PATH, FEW_SHOTS_PATH, SYMPTOM_MAP_PATH
from utils.helpers import load_json, load_text, get_logger

logger = get_logger(__name__)


class PromptBuilder:
    """Builds a chat-formatted prompt for Qwen2-VL."""

    def __init__(self):
        self._system_prompt: str = self._load_system_prompt()
        self._few_shots: list[dict] = self._load_few_shots()
        self._symptom_map: dict = self._load_symptom_map()

    # ── Public API ────────────────────────────────────────────────────────────

    def build(
        self,
        user_text: str,
        image_tokens: Optional[str] = None,
        extra_context: Optional[dict] = None,
    ) -> list[dict]:
        """
        Return a list of message dicts compatible with llama-cpp-python's
        `create_chat_completion` interface.

        Parameters
        ----------
        user_text     : Free-text symptom description from the user.
        image_tokens  : Pre-formatted image token string produced by vision/encoder.py.
        extra_context : Optional dict with additional structured fields
                        (e.g. age, region, duration).

        Returns
        -------
        List of {"role": ..., "content": ...} dicts.
        """
        messages: list[dict] = []

        # 1. System turn
        messages.append({"role": "system", "content": self._system_prompt})

        # 2. Few-shot examples
        for shot in self._few_shots:
            messages.append({"role": "user",      "content": shot["user"]})
            messages.append({"role": "assistant",  "content": shot["assistant"]})

        # 3. Current user turn
        user_content = self._compose_user_content(user_text, image_tokens, extra_context)
        messages.append({"role": "user", "content": user_content})

        logger.debug("Built prompt with %d turns.", len(messages))
        return messages

    # ── Helpers ───────────────────────────────────────────────────────────────

    def _compose_user_content(
        self,
        user_text: str,
        image_tokens: Optional[str],
        extra_context: Optional[dict],
    ) -> str:
        parts: list[str] = []

        if image_tokens:
            parts.append(image_tokens)

        if extra_context:
            ctx_lines = ["[Patient Context]"]
            for k, v in extra_context.items():
                ctx_lines.append(f"  {k}: {v}")
            parts.append("\n".join(ctx_lines))

        # Enrich user text with known symptom aliases
        enriched = self._enrich_symptoms(user_text)
        parts.append(f"[Symptoms / Complaint]\n{enriched}")

        parts.append(
            "Please analyse the above information and provide:\n"
            "1. Most likely diagnosis\n"
            "2. Confidence (0–100%)\n"
            "3. Supporting reasoning\n"
            "4. Recommended next steps\n"
            "5. Urgency level (low / medium / high)"
        )

        return "\n\n".join(parts)

    def _enrich_symptoms(self, text: str) -> str:
        """
        Replace colloquial symptom terms with their canonical medical names
        using symptom_map.json. Case-insensitive.
        """
        lower = text.lower()
        for colloquial, canonical in self._symptom_map.items():
            if colloquial in lower:
                text = text.replace(colloquial, f"{colloquial} ({canonical})")
        return text

    # ── Loaders ───────────────────────────────────────────────────────────────

    @staticmethod
    def _load_system_prompt() -> str:
        try:
            return load_text(SYSTEM_PROMPT_PATH)
        except FileNotFoundError:
            logger.warning("system_prompt.txt not found — using default.")
            return (
                "You are SehatSaathi, a careful AI medical assistant for rural India. "
                "You provide preliminary health assessments in simple language. "
                "Always advise the patient to see a qualified doctor for confirmation. "
                "Never prescribe medications. Output structured JSON."
            )

    @staticmethod
    def _load_few_shots() -> list[dict]:
        try:
            return load_json(FEW_SHOTS_PATH)
        except FileNotFoundError:
            logger.warning("few_shots.json not found — no examples injected.")
            return []

    @staticmethod
    def _load_symptom_map() -> dict:
        try:
            return load_json(SYMPTOM_MAP_PATH)
        except FileNotFoundError:
            logger.warning("symptom_map.json not found — no enrichment.")
            return {}