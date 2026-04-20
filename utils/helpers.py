"""
utils/helpers.py — Small, reusable utility functions.
"""

import json
import logging
import time
from pathlib import Path
from typing import Any

import psutil

logger = logging.getLogger(__name__)


# ── Logging setup ─────────────────────────────────────────────────────────────

def get_logger(name: str, level: int = logging.INFO) -> logging.Logger:
    """Return a consistently formatted logger."""
    log = logging.getLogger(name)
    if not log.handlers:
        handler = logging.StreamHandler()
        handler.setFormatter(
            logging.Formatter("[%(asctime)s] %(levelname)s %(name)s — %(message)s",
                              datefmt="%H:%M:%S")
        )
        log.addHandler(handler)
    log.setLevel(level)
    return log


# ── File I/O ──────────────────────────────────────────────────────────────────

def load_json(path: Path | str) -> Any:
    """Load and return a JSON file. Raises FileNotFoundError if missing."""
    path = Path(path)
    if not path.exists():
        raise FileNotFoundError(f"JSON file not found: {path}")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def load_text(path: Path | str) -> str:
    """Load and return a plain-text file."""
    path = Path(path)
    if not path.exists():
        raise FileNotFoundError(f"Text file not found: {path}")
    return path.read_text(encoding="utf-8").strip()


# ── System / memory ───────────────────────────────────────────────────────────

def available_ram_bytes() -> int:
    """Return available system RAM in bytes."""
    return psutil.virtual_memory().available


def total_ram_bytes() -> int:
    """Return total system RAM in bytes."""
    return psutil.virtual_memory().total


def ram_summary() -> str:
    """Human-readable RAM summary."""
    vm = psutil.virtual_memory()
    return (f"RAM total={vm.total / 1e9:.1f}GB  "
            f"available={vm.available / 1e9:.1f}GB  "
            f"used={vm.percent:.0f}%")


# ── Timing ────────────────────────────────────────────────────────────────────

class Timer:
    """Context manager that records elapsed time."""

    def __init__(self, label: str = ""):
        self.label = label
        self.elapsed: float = 0.0

    def __enter__(self):
        self._start = time.perf_counter()
        return self

    def __exit__(self, *_):
        self.elapsed = time.perf_counter() - self._start
        if self.label:
            logger.debug("%s took %.3fs", self.label, self.elapsed)


# ── Misc ──────────────────────────────────────────────────────────────────────

def clamp(value: float, lo: float = 0.0, hi: float = 1.0) -> float:
    """Clamp a float between lo and hi."""
    return max(lo, min(hi, value))


def truncate(text: str, max_chars: int = 200) -> str:
    """Truncate a string for safe logging."""
    if len(text) <= max_chars:
        return text
    return text[:max_chars] + "…"