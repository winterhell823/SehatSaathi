"""
app/main.py — CLI and FastAPI entry point for SehatSaathi-AI.

Usage (CLI)
-----------
    python -m app.main --text "I have fever and cough for 3 days"
    python -m app.main --text "Skin rash on arm" --image rash.jpg
    python -m app.main --text "..." --age 45 --region "Rajasthan" --duration "5 days"

Usage (API server)
------------------
    python -m app.main --serve
    # then POST to http://localhost:8000/diagnose
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

# ── FastAPI imports (lazy — only required in --serve mode) ────────────────────
def _get_app():
    from fastapi import FastAPI, UploadFile, File, Form, HTTPException
    from fastapi.middleware.cors import CORSMiddleware
    import uvicorn
    import shutil
    import tempfile

    from core.pipeline import run
    from utils.config import API_HOST, API_PORT

    app = FastAPI(
        title="SehatSaathi-AI",
        description="Preliminary health assessment for low-resource devices.",
        version="1.0.0",
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @app.get("/health")
    def health():
        from utils.helpers import ram_summary
        from core.model_loader import get_loader
        loader = get_loader()
        return {
            "status": "ok",
            "model":  loader.model_name if loader.is_loaded else "not_loaded",
            "ram":    ram_summary(),
        }

    @app.post("/diagnose")
    async def diagnose(
        text:     str        = Form(...),
        age:      str | None = Form(None),
        region:   str | None = Form(None),
        duration: str | None = Form(None),
        image:    UploadFile  = File(None),
    ):
        extra = {}
        if age:      extra["age"]      = age
        if region:   extra["region"]   = region
        if duration: extra["duration"] = duration

        image_path = None
        tmp_file   = None

        try:
            if image:
                suffix   = Path(image.filename).suffix or ".jpg"
                tmp_file = tempfile.NamedTemporaryFile(delete=False, suffix=suffix)
                shutil.copyfileobj(image.file, tmp_file)
                tmp_file.close()
                image_path = tmp_file.name

            result = run(
                symptom_text  = text,
                image_path    = image_path,
                extra_context = extra or None,
            )
            return result.to_dict()

        except Exception as exc:
            raise HTTPException(status_code=500, detail=str(exc))

        finally:
            if tmp_file:
                Path(tmp_file.name).unlink(missing_ok=True)

    return app, uvicorn, API_HOST, API_PORT


# ── CLI logic ─────────────────────────────────────────────────────────────────

def _cli(args: argparse.Namespace):
    from core.pipeline import run

    extra = {}
    if args.age:      extra["age"]      = args.age
    if args.region:   extra["region"]   = args.region
    if args.duration: extra["duration"] = args.duration

    print("\n🩺  SehatSaathi-AI — analysing…\n")

    if args.stream:
        # Streaming mode — print tokens as they arrive
        from core.model_loader import get_model
        from core.prompt_builder import PromptBuilder
        from core.inference import InferenceEngine
        from vision.preprocess import preprocess_image
        from vision.encoder import encode_image

        model = get_model()
        if model is None:
            print("⚠️  No GGUF model available.  Falling back to non-streaming mode.")
            args.stream = False
        else:
            image_tokens = None
            if args.image:
                arr = preprocess_image(args.image)
                image_tokens = encode_image(arr)

            pb  = PromptBuilder()
            eng = InferenceEngine(model)
            msgs = pb.build(args.text, image_tokens=image_tokens, extra_context=extra or None)

            print("Assistant: ", end="", flush=True)
            for chunk in eng.run_streaming(msgs):
                print(chunk, end="", flush=True)
            print("\n")
            return

    result = run(
        symptom_text  = args.text,
        image_path    = args.image,
        extra_context = extra or None,
    )

    if args.json:
        print(json.dumps(result.to_dict(), indent=2, ensure_ascii=False))
    else:
        d = result.to_dict()
        print(f"  Diagnosis    : {d['diagnosis']}")
        print(f"  Confidence   : {d['confidence']*100:.0f}%  [{d['confidence_label']}]")
        print(f"  Urgency      : {d['urgency']}")
        print(f"  Reasoning    : {d['reasoning']}")
        print(f"  Next Steps   :")
        for step in d["next_steps"]:
            print(f"    • {step}")
        print()


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        prog="sehatsaathi",
        description="SehatSaathi-AI — on-device health assistant",
    )
    parser.add_argument("--text",     required=False, help="Symptom description")
    parser.add_argument("--image",    required=False, help="Path to skin/wound image")
    parser.add_argument("--age",      required=False, help="Patient age")
    parser.add_argument("--region",   required=False, help="Patient region/state")
    parser.add_argument("--duration", required=False, help="Duration of symptoms")
    parser.add_argument("--json",     action="store_true", help="Output raw JSON")
    parser.add_argument("--stream",   action="store_true", help="Stream tokens (CLI)")
    parser.add_argument("--serve",    action="store_true", help="Start FastAPI server")

    args = parser.parse_args()

    if args.serve:
        app, uvicorn, host, port = _get_app()
        print(f"🚀  Starting SehatSaathi-AI API on http://{host}:{port}")
        uvicorn.run(app, host=host, port=port)

    elif args.text:
        _cli(args)

    else:
        parser.print_help()
        sys.exit(1)


if __name__ == "__main__":
    main()