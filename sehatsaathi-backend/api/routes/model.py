from fastapi import APIRouter, HTTPException
from fastapi.responses import FileResponse
from db.mongo_client import get_db
from db.collections import MODEL_VERSIONS
import os

router = APIRouter()

@router.get("/latest")
async def get_latest_model():
    db = get_db()
    latest = await db[MODEL_VERSIONS].find_one(
        {"status": "ready"},
        sort=[("version", -1)]
    )
    if not latest:
        return {"version": "1.0.0", "checksum": "none", "size_mb": 0}
    return {
        "version":     latest["version"],
        "checksum":    latest.get("checksum", ""),
        "size_mb":     latest.get("size_mb", 0),
        "released_at": latest.get("createdAt")
    }

@router.get("/download")
async def download_model():
    model_path = os.getenv("MODEL_FILE_PATH", "models/diagnosis_model.tflite")
    if not os.path.exists(model_path):
        raise HTTPException(status_code=404, detail="Model file not found")
    return FileResponse(
        path=model_path,
        media_type="application/octet-stream",
        filename="diagnosis_model.tflite"
    )