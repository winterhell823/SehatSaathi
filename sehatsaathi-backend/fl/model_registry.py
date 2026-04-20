from db.mongo_client import get_sync_db, get_db
from db.collections import MODEL_VERSIONS
from datetime import datetime
import os

def get_latest_version_sync() -> dict:
    db = get_sync_db()
    latest = db[MODEL_VERSIONS].find_one(
        {"status": "ready"},
        sort=[("version", -1)]
    )
    if not latest:
        return {"version": "1.0.0", "checksum": "", "size_mb": 0}
    latest.pop("_id", None)
    return latest

async def get_latest_version_async() -> dict:
    db = get_db()
    latest = await db[MODEL_VERSIONS].find_one(
        {"status": "ready"},
        sort=[("version", -1)]
    )
    if not latest:
        return {"version": "1.0.0", "checksum": "", "size_mb": 0}
    latest.pop("_id", None)
    return latest

def bump_version(current: str) -> str:
    parts   = current.split(".")
    new_ver = f"{parts[0]}.{parts[1]}.{int(parts[2]) + 1}"
    return new_ver

def register_new_round(client_count: int, total_samples: int) -> str:
    db             = get_sync_db()
    latest         = get_latest_version_sync()
    new_version    = bump_version(latest["version"])

    db[MODEL_VERSIONS].insert_one({
        "version":      new_version,
        "status":       "aggregating",
        "clientCount":  client_count,
        "totalSamples": total_samples,
        "createdAt":    datetime.utcnow()
    })

    print(f"[model-registry] New round registered | version={new_version} | clients={client_count}")
    return new_version

def get_model_path(version: str) -> str:
    db    = get_sync_db()
    doc   = db[MODEL_VERSIONS].find_one({"version": version, "status": "ready"})
    if not doc:
        raise FileNotFoundError(f"Model version {version} not found or not ready")
    return doc["model_path"]

def list_versions(limit: int = 10) -> list:
    db      = get_sync_db()
    cursor  = db[MODEL_VERSIONS].find(
        {},
        {"_id": 0, "version": 1, "status": 1, "clientCount": 1, "createdAt": 1}
    ).sort("version", -1).limit(limit)
    return list(cursor)