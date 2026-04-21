"""
utils/config.py — Central configuration for SehatSaathi-AI.
All paths, RAM thresholds, and tunable constants live here.
"""

import os
from pathlib import Path

# ── Directory roots ──────────────────────────────────────────────────────────
BASE_DIR   = Path(__file__).resolve().parent.parent
MODEL_DIR  = BASE_DIR / "models"
DATA_DIR   = BASE_DIR / "data"

# ── Model paths ──────────────────────────────────────────────────────────────
QWEN_DIR          = MODEL_DIR / "qwen"
QWEN_Q4_PATH      = QWEN_DIR  / "qwen2-vl-2b-q4_k_m.gguf"
QWEN_Q2_PATH      = QWEN_DIR  / "qwen2-vl-2b-q2_k.gguf"
QWEN_CONFIG_PATH  = QWEN_DIR  / "model_config.json"

TFLITE_DIR          = MODEL_DIR / "tflite"
TFLITE_MODEL_PATH   = TFLITE_DIR / "skin_classifier.tflite"
TFLITE_LABELS_PATH  = TFLITE_DIR / "label_map.json"

# ── Data / knowledge paths ────────────────────────────────────────────────────
KNOWLEDGE_DIR      = DATA_DIR / "knowledge"
DISEASE_CHUNKS_PATH = KNOWLEDGE_DIR / "disease_chunks.json"
SYMPTOM_MAP_PATH    = KNOWLEDGE_DIR / "symptom_map.json"
PRIORS_PATH         = KNOWLEDGE_DIR / "priors.json"

PROMPTS_DIR        = DATA_DIR / "prompts"
SYSTEM_PROMPT_PATH = PROMPTS_DIR / "system_prompt.txt"
FEW_SHOTS_PATH     = PROMPTS_DIR / "few_shots.json"

# ── RAM thresholds (bytes) ────────────────────────────────────────────────────
RAM_THRESHOLD_Q4   = 3 * 1024 ** 3   # ≥ 3 GB  → use Q4_K_M
RAM_THRESHOLD_Q2   = 2 * 1024 ** 3   # ≥ 2 GB  → use Q2_K
# < 2 GB  → TFLite fallback

# ── Inference defaults ────────────────────────────────────────────────────────
MAX_NEW_TOKENS     = 512
TEMPERATURE        = 0.2
TOP_P              = 0.9
REPEAT_PENALTY     = 1.1
N_CTX              = 2048          # context window
N_THREADS          = max(1, (os.cpu_count() or 2) - 1)

# ── Vision / image pre-processing ────────────────────────────────────────────
IMAGE_SIZE         = (448, 448)    # Qwen2-VL native resolution
TFLITE_IMAGE_SIZE  = (224, 224)    # MobileNet-style TFLite input
IMAGE_MEAN         = (0.485, 0.456, 0.406)
IMAGE_STD          = (0.229, 0.224, 0.225)

# ── Confidence thresholds ─────────────────────────────────────────────────────
CONFIDENCE_HIGH    = 0.75
CONFIDENCE_LOW     = 0.40

# ── API server ────────────────────────────────────────────────────────────────
API_HOST           = os.getenv("SEHATSAATHI_HOST", "0.0.0.0")
API_PORT           = int(os.getenv("SEHATSAATHI_PORT", "8000"))