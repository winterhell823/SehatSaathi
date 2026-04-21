from db.mongo_client import get_db
from db.collections import (
    ENCOUNTERS, PATIENTS, DRUG_AVAILABILITY,
    KNOWLEDGE_CHUNKS, MODEL_VERSIONS
)
from pymongo import ASCENDING, DESCENDING, TEXT

async def create_indexes():
    db = get_db()

    await db[ENCOUNTERS].create_index(
        [("patientId", ASCENDING), ("timestamp", DESCENDING)]
    )
    await db[ENCOUNTERS].create_index([("synced", ASCENDING)])
    await db[ENCOUNTERS].create_index([("chwId", ASCENDING)])

    await db[PATIENTS].create_index(
        [("patientId", ASCENDING)], unique=True
    )
    await db[PATIENTS].create_index([("name", TEXT)])
    await db[PATIENTS].create_index([("village", ASCENDING)])

    await db[DRUG_AVAILABILITY].create_index(
        [("district", ASCENDING), ("drug_name", ASCENDING)]
    )

    await db[MODEL_VERSIONS].create_index(
        [("version", DESCENDING)]
    )

    print("MongoDB indexes created successfully")