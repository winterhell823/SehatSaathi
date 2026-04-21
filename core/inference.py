"""
core/inference.py — Runs a chat completion through the loaded Qwen2-VL model.
"""

from __future__ import annotations

import logging
from typing import Optional

from llama_cpp import Llama

from utils.config import MAX_NEW_TOKENS, TEMPERATURE, TOP_P, REPEAT_PENALTY
from utils.helpers import Timer, get_logger, truncate

logger = get_logger(__name__)


class InferenceEngine:
    """
    Thin wrapper around llama-cpp-python's `create_chat_completion`.
    Keeps inference concerns separate from prompt-building and parsing.
    """

    def __init__(self, model: Llama):
        self._model = model

    # ── Public API ────────────────────────────────────────────────────────────

    def run(
        self,
        messages: list[dict],
        max_new_tokens: int = MAX_NEW_TOKENS,
        temperature: float = TEMPERATURE,
        top_p: float = TOP_P,
        repeat_penalty: float = REPEAT_PENALTY,
    ) -> str:
        """
        Run chat completion and return the raw assistant response string.

        Parameters
        ----------
        messages        : List of role/content dicts from PromptBuilder.
        max_new_tokens  : Maximum tokens to generate.
        temperature     : Sampling temperature.
        top_p           : Nucleus sampling cutoff.
        repeat_penalty  : Penalise repetition.

        Returns
        -------
        Raw text of the assistant's response.
        """
        logger.debug("Sending %d messages to model.", len(messages))

        with Timer("inference") as t:
            result = self._model.create_chat_completion(
                messages=messages,
                max_tokens=max_new_tokens,
                temperature=temperature,
                top_p=top_p,
                repeat_penalty=repeat_penalty,
                stream=False,
            )

        raw: str = result["choices"][0]["message"]["content"]
        usage = result.get("usage", {})

        logger.info(
            "Inference done in %.2fs | prompt_tokens=%s generated_tokens=%s",
            t.elapsed,
            usage.get("prompt_tokens", "?"),
            usage.get("completion_tokens", "?"),
        )
        logger.debug("Raw response: %s", truncate(raw))
        return raw

    def run_streaming(
        self,
        messages: list[dict],
        max_new_tokens: int = MAX_NEW_TOKENS,
        temperature: float = TEMPERATURE,
    ):
        """
        Generator that yields response chunks as they stream from the model.
        Useful for real-time CLI display.
        """
        for chunk in self._model.create_chat_completion(
            messages=messages,
            max_tokens=max_new_tokens,
            temperature=temperature,
            stream=True,
        ):
            delta = chunk["choices"][0].get("delta", {})
            content = delta.get("content", "")
            if content:
                yield content