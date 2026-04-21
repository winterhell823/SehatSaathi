"""
core/pipeline.py — Full inference pipeline.

Orchestrates: model loading → vision encoding → prompt building
              → inference → response parsing → result.

This is the single entry point for all downstream callers
(CLI, API, tests).
"""

from __future__ import annotations

from pathlib import Path
from typing import Optional

from core.model_loader import get_loader, get_model
from core.prompt_builder import PromptBuilder
from core.inference import InferenceEngine
from core.response_parser import ResponseParser, DiagnosisResult
from vision.preprocess import preprocess_image
from vision.encoder import encode_image
from vision.fallback import TFLiteClassifier
from utils.helpers import get_logger, Timer

logger = get_logger(__name__)

# Module-level singletons (built once, reused)
_prompt_builder  = PromptBuilder()
_response_parser = ResponseParser()
_tflite          = TFLiteClassifier()


def run(
    symptom_text: str,
    image_path: Optional[str | Path] = None,
    extra_context: Optional[dict] = None,
) -> DiagnosisResult:
    """
    Run the full SehatSaathi pipeline.

    Parameters
    ----------
    symptom_text  : User-described symptoms (plain text).
    image_path    : Optional path to a skin / wound image.
    extra_context : Optional dict with structured patient context
                    (e.g. {"age": 34, "region": "Bihar", "duration": "3 days"}).

    Returns
    -------
    DiagnosisResult — structured diagnosis with confidence, urgency, etc.
    """
    with Timer("full_pipeline") as total:

        # ── 1. Load model ─────────────────────────────────────────────────────
        loader = get_loader()
        model  = get_model()          # None if RAM too low

        # ── 2. Handle image (if provided) ─────────────────────────────────────
        image_tokens: Optional[str] = None

        if image_path is not None:
            image_path = Path(image_path)
            logger.info("Processing image: %s", image_path.name)

            if model is None:
                # Low-RAM path: use TFLite classifier only
                logger.info("Using TFLite fallback for image classification.")
                return _tflite_only_result(image_path, symptom_text)

            # Full path: encode image for Qwen2-VL
            img_array = preprocess_image(image_path)
            image_tokens = encode_image(img_array)

        # ── 3. Text-only or combined path ─────────────────────────────────────
        if model is None:
            logger.warning(
                "No GGUF model loaded and no image supplied — "
                "returning low-confidence fallback."
            )
            return _text_fallback_result(symptom_text)

        # ── 4. Build prompt ────────────────────────────────────────────────────
        messages = _prompt_builder.build(
            user_text=symptom_text,
            image_tokens=image_tokens,
            extra_context=extra_context,
        )

        # ── 5. Run inference ───────────────────────────────────────────────────
        engine = InferenceEngine(model)
        raw_response = engine.run(messages)

        # ── 6. Parse response ──────────────────────────────────────────────────
        result = _response_parser.parse(raw_response)

    logger.info(
        "Pipeline complete in %.2fs | diagnosis=%s | confidence=%.0f%% | urgency=%s",
        total.elapsed,
        result.diagnosis,
        result.confidence * 100,
        result.urgency,
    )
    return result


# ── Internal helpers ──────────────────────────────────────────────────────────

def _tflite_only_result(image_path: Path, symptom_text: str) -> DiagnosisResult:
    """
    When RAM is too low for GGUF, classify the image via TFLite and return
    a result without LLM reasoning.
    """
    label, confidence = _tflite.classify(image_path)

    return DiagnosisResult(
        diagnosis=label,
        confidence=confidence,
        reasoning=(
            "Classified by lightweight TFLite model (limited RAM mode). "
            "LLM reasoning unavailable. Please consult a doctor for confirmation."
        ),
        next_steps=["Visit a qualified healthcare provider for a full assessment."],
        urgency="unknown",
        raw_response=f"TFLite: {label} ({confidence:.0%})",
        parse_method="tflite",
    )


def _text_fallback_result(symptom_text: str) -> DiagnosisResult:
    """
    When no model is available and no image is provided, return a safe
    fallback directing the user to seek care.
    """
    return DiagnosisResult(
        diagnosis="Insufficient data",
        confidence=0.0,
        reasoning="No model could be loaded on this device and no image was provided.",
        next_steps=["Please visit a local healthcare provider or ASHA worker."],
        urgency="medium",
        raw_response="",
        parse_method="fallback",
    )
