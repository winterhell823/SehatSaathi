"""
vision/encoder.py — Convert a preprocessed image array into the token string
that Qwen2-VL expects in the prompt.

Qwen2-VL uses a special <img>…</img> tag with a base64-encoded JPEG payload.
The model's vision tower processes these tokens internally during inference.
"""

from __future__ import annotations

import base64
import io
import logging

import numpy as np
from PIL import Image

from utils.helpers import get_logger

logger = get_logger(__name__)

# Qwen2-VL image token format (matches the model's tokenizer vocabulary)
_IMG_START  = "<img>"
_IMG_END    = "</img>"


def encode_image(img_array: np.ndarray) -> str:
    """
    Encode a float32 HWC image array as a Qwen2-VL image token string.

    The array is expected to be the result of `preprocess_image` with
    normalise=False (i.e., values in [0, 1]) OR we undo the normalisation
    before JPEG encoding.

    Parameters
    ----------
    img_array : float32 array of shape (H, W, 3).

    Returns
    -------
    String of the form "<img>BASE64_JPEG</img>".
    """
    pil_img = _array_to_pil(img_array)
    b64     = _pil_to_base64_jpeg(pil_img)
    token   = f"{_IMG_START}{b64}{_IMG_END}"
    logger.debug("Image encoded — token length: %d chars", len(token))
    return token


def encode_image_path(image_path: str) -> str:
    """
    Convenience wrapper: load raw PIL, resize to 448×448, then encode.
    Skips numpy normalisation (JPEG encoding doesn't need it).
    """
    from utils.config import IMAGE_SIZE
    img = Image.open(image_path).convert("RGB").resize(IMAGE_SIZE, Image.LANCZOS)
    b64 = _pil_to_base64_jpeg(img)
    return f"{_IMG_START}{b64}{_IMG_END}"


# ── Private helpers ───────────────────────────────────────────────────────────

def _array_to_pil(arr: np.ndarray) -> Image.Image:
    """
    Convert a float32 [0,1] or normalised array back to a uint8 PIL image.
    Handles both normalised (ImageNet mean/std) and plain [0,1] arrays.
    """
    from utils.config import IMAGE_MEAN, IMAGE_STD

    a = arr.copy()

    # Detect if ImageNet-normalised: values likely go below 0
    if a.min() < 0:
        mean = np.array(IMAGE_MEAN, dtype=np.float32)
        std  = np.array(IMAGE_STD,  dtype=np.float32)
        a    = a * std + mean

    a = np.clip(a * 255.0, 0, 255).astype(np.uint8)
    return Image.fromarray(a, mode="RGB")


def _pil_to_base64_jpeg(img: Image.Image, quality: int = 85) -> str:
    """Encode a PIL image as a base64 JPEG string."""
    buf = io.BytesIO()
    img.save(buf, format="JPEG", quality=quality, optimize=True)
    return base64.b64encode(buf.getvalue()).decode("ascii")