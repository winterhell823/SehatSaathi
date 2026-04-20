# 🩺 SehatSaathi-AI

> On-device, low-resource AI health assistant for rural India.  
> Runs Qwen2-VL-2B (GGUF) locally — no internet required after setup.

---

## Project Structure

```
sehatsaathi-ai/
├── models/           # GGUF and TFLite model files (download separately)
├── data/             # Knowledge base and prompt templates
├── core/             # Main inference pipeline
├── vision/           # Image preprocessing and encoding
├── utils/            # Config and helpers
├── app/              # CLI and FastAPI server
└── requirements.txt
```

---

## Quick Start

### 1. Install dependencies

```bash
pip install -r requirements.txt
```

> **Note:** `llama-cpp-python` must be compiled for your hardware.  
> For CPU-only: `pip install llama-cpp-python`  
> For GPU: see [llama-cpp-python docs](https://github.com/abetlen/llama-cpp-python)

### 2. Download models

Place the following files manually (not included in repo due to size):

| Path | Source |
|------|--------|
| `models/qwen/qwen2-vl-2b-q4_k_m.gguf` | HuggingFace — Qwen2-VL-2B-Instruct-GGUF |
| `models/qwen/qwen2-vl-2b-q2_k.gguf`   | HuggingFace — Qwen2-VL-2B-Instruct-GGUF |
| `models/tflite/skin_classifier.tflite` | Your trained MobileNet model |

### 3. Run (CLI)

```bash
# Text-only
python -m app.main --text "Fever for 3 days and body aches"

# With image
python -m app.main --text "Rash on arm" --image rash.jpg

# With patient context
python -m app.main \
  --text "Khansi aur bukhar hai" \
  --age 28 \
  --region "Bihar" \
  --duration "5 days"

# JSON output
python -m app.main --text "..." --json

# Streaming tokens
python -m app.main --text "..." --stream
```

### 4. Run as API server

```bash
python -m app.main --serve
# API available at http://localhost:8000
# Docs at http://localhost:8000/docs
```

**POST /diagnose** (multipart/form-data)

| Field | Type | Required |
|-------|------|----------|
| `text` | string | ✅ |
| `image` | file | ❌ |
| `age` | string | ❌ |
| `region` | string | ❌ |
| `duration` | string | ❌ |

---

## RAM Tier Logic

| Available RAM | Model Used |
|---|---|
| ≥ 3 GB | `qwen2-vl-2b-q4_k_m.gguf` (best quality) |
| ≥ 2 GB | `qwen2-vl-2b-q2_k.gguf` (reduced quality) |
| < 2 GB | `skin_classifier.tflite` (image only, no LLM reasoning) |

---

## Output Format

```json
{
  "diagnosis":        "Malaria (Plasmodium falciparum suspected)",
  "confidence":       0.78,
  "confidence_label": "high",
  "reasoning":        "High fever with chills and myalgia in monsoon season...",
  "next_steps":       ["Visit nearest PHC for RDT test", "Use mosquito net"],
  "urgency":          "high",
  "parse_method":     "json"
}
```

---

## ⚠️ Disclaimer

SehatSaathi-AI provides **preliminary assessments only**. It is **not a substitute for professional medical diagnosis or treatment**. Always advise patients to consult a qualified healthcare provider, especially for serious symptoms.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SEHATSAATHI_HOST` | `0.0.0.0` | API server host |
| `SEHATSAATHI_PORT` | `8000` | API server port |