from motor.motor_asyncio import AsyncIOMotorClient
from pymongo import MongoClient
import os

_async_client: AsyncIOMotorClient = None
_db = None

async def connect_db():
    global _async_client, _db
    uri = os.getenv("MONGO_URI", "mongodb://localhost:27017")
    _async_client = AsyncIOMotorClient(uri)
    _db = _async_client[os.getenv("MONGO_DB_NAME", "vitalai")]
    print(f"Connected to MongoDB: {uri}")

async def close_db():
    global _async_client
    if _async_client:
        _async_client.close()

def get_db():
    return _db

def get_sync_db():
    uri = os.getenv("MONGO_URI", "mongodb://localhost:27017")
    client = MongoClient(uri)
    return client[os.getenv("MONGO_DB_NAME", "vitalai")]