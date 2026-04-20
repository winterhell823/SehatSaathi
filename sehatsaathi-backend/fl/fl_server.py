from db.mongo_client import get_sync_db
from db.collections import MODEL_VERSIONS
from datetime import datetime
import os
import hashlib
import base64

MIN_CLIENTS    = int(os.getenv("FL_MIN_CLIENTS", "5"))
MODEL_SAVE_DIR = os.getenv("MODEL_SAVE_DIR", "models")

def federated_average(gradients: list) -> dict:
    total_samples = sum(g["num_samples"] for g in gradients)
    averaged = {}

    for gradient in gradients:
        weight = gradient["num_samples"] / total_samples
        payload = gradient["gradient_payload"]

        for key, value in payload.items():
            if key not in averaged:
                averaged[key] = 0.0
            averaged[key] += value * weight

    return averaged

def compute_checksum(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def save_model(version: str, model_bytes: bytes) -> str:
    os.makedirs(MODEL_SAVE_DIR, exist_ok=True)
    path = os.path.join(MODEL_SAVE_DIR, f"model_v{version}.tflite")
    with open(path, "wb") as f:
        f.write(model_bytes)
    return path

def run_aggregation(gradients: list, new_version: str):
    print(f"[fl-server] Starting FedAvg | clients={len(gradients)} | version={new_version}")

    try:
        averaged_weights = federated_average(gradients)
        model_bytes      = base64.b64encode(str(averaged_weights).encode())
        checksum         = compute_checksum(model_bytes)
        model_path       = save_model(new_version, model_bytes)
        size_mb          = os.path.getsize(model_path) / (1024 * 1024)

        db = get_sync_db()
        db[MODEL_VERSIONS].update_one(
            {"version": new_version},
            {"$set": {
                "status":      "ready",
                "checksum":    checksum,
                "size_mb":     round(size_mb, 2),
                "model_path":  model_path,
                "completedAt": datetime.utcnow()
            }}
        )
        print(f"[fl-server] Aggregation complete | version={new_version} | checksum={checksum[:16]}...")

    except Exception as e:
        db = get_sync_db()
        db[MODEL_VERSIONS].update_one(
            {"version": new_version},
            {"$set": {"status": "failed", "error": str(e)}}
        )
        print(f"[fl-server] Aggregation failed | version={new_version} | error={e}")