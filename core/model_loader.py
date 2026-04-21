"""
core/model_loader.py — Selects and loads the correct GGUF model based on
available RAM, exposing a single Llama instance to the rest of the system.
"""

from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Optional

from llama_cpp import Llama

from utils.config import (
    QWEN_Q4_PATH, QWEN_Q2_PATH, QWEN_CONFIG_PATH,
    RAM_THRESHOLD_Q4, RAM_THRESHOLD_Q2,
    MAX_NEW_TOKENS, N_CTX, N_THREADS,
)
from utils.helpers import available_ram_bytes, ram_summary, get_logger

logger = get_logger(__name__)


class ModelLoader:
    """
    Picks the best GGUF variant that fits in available RAM and loads it
    via llama-cpp-python.

    Tier logic
    ----------
    ≥ 3 GB free  → Q4_K_M  (best quality)
    ≥ 2 GB free  → Q2_K    (acceptable quality)
    < 2 GB free  → None    (caller should use TFLite fallback)
    """

    def __init__(self):
        self._model: Optional[Llama] = None
        self._model_path: Optional[Path] = None
        self._config: dict = {}

    # ── Public API ────────────────────────────────────────────────────────────

    def load(self) -> Optional[Llama]:
        """
        Load the best available model. Returns the Llama instance or None
        if RAM is insufficient for any GGUF model.
        """
        if self._model is not None:
            return self._model

        logger.info("System memory: %s", ram_summary())
        path = self._select_model_path()

        if path is None:
            logger.warning("Insufficient RAM for any GGUF model — TFLite fallback required.")
            return None

        self._config = self._load_config()
        logger.info("Loading model from: %s", path)

        self._model = Llama(
            model_path=str(path),
            n_ctx=N_CTX,
            n_threads=N_THREADS,
            n_gpu_layers=self._config.get("n_gpu_layers", 0),
            verbose=False,
        )
        self._model_path = path
        logger.info("Model loaded successfully (%s)", path.name)
        return self._model

    @property
    def is_loaded(self) -> bool:
        return self._model is not None

    @property
    def model_name(self) -> str:
        if self._model_path:
            return self._model_path.name
        return "none"

    def unload(self):
        """Explicitly free the model from memory."""
        if self._model is not None:
            del self._model
            self._model = None
            logger.info("Model unloaded.")

    # ── Private helpers ───────────────────────────────────────────────────────

    def _select_model_path(self) -> Optional[Path]:
        ram = available_ram_bytes()
        logger.debug("Available RAM: %.2f GB", ram / 1e9)

        if ram >= RAM_THRESHOLD_Q4 and QWEN_Q4_PATH.exists():
            logger.info("Selected Q4_K_M model (%.1f GB free)", ram / 1e9)
            return QWEN_Q4_PATH

        if ram >= RAM_THRESHOLD_Q2 and QWEN_Q2_PATH.exists():
            logger.info("Selected Q2_K model (%.1f GB free)", ram / 1e9)
            return QWEN_Q2_PATH

        return None

    def _load_config(self) -> dict:
        if QWEN_CONFIG_PATH.exists():
            with open(QWEN_CONFIG_PATH, "r") as f:
                return json.load(f)
        return {}


# ── Module-level singleton ────────────────────────────────────────────────────

_loader = ModelLoader()


def get_model() -> Optional[Llama]:
    """Return the loaded Llama model (loads on first call)."""
    return _loader.load()


def get_loader() -> ModelLoader:
    """Return the singleton ModelLoader."""
    return _loader