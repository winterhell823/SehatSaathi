# db/mongo_client.py
import os
from pymongo import MongoClient
from dotenv import load_dotenv


load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), '..', '.env'))

_client = None

def get_client():
    global _client
    if _client is None:
        _client = MongoClient(os.environ["MONGO_URI"])
    return _client

def get_db():
    return get_client()[os.environ["MONGO_DB_NAME"]]