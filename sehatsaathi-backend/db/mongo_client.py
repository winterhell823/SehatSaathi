import os
from motor.motor_asyncio import AsyncIOMotorClient
from pymongo import MongoClient

_async_client = None
_db = None

async def connect_db():
    global _async_client, _db
    uri = os.environ.get("MONGO_URI", "mongodb://localhost:27017")
    db_name = os.environ.get("MONGO_DB_NAME", "vitalai")
    
    _async_client = AsyncIOMotorClient(uri)
    _db = _async_client[db_name]
    print(f"Connected to MongoDB: {uri} / {db_name}")

async def close_db():
    global _async_client
    if _async_client:
        _async_client.close()

def get_db():
    return _db

def get_sync_db():
    uri = os.environ.get("MONGO_URI", "mongodb://localhost:27017")
    db_name = os.environ.get("MONGO_DB_NAME", "vitalai")
    client = MongoClient(uri)
    return client[db_name]
