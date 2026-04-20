from db.mongo_client import get_db
from db.collections import DRUG_AVAILABILITY

async def map_treatment(diagnosis: str, district: str, state: str) -> dict:
    db = get_db()

    condition_key = diagnosis.lower().replace(" ", "_")

    cursor = db[DRUG_AVAILABILITY].find(
        {
            "condition": condition_key,
            "district":  district.lower()
        },
        {"_id": 0}
    )
    drugs = await cursor.to_list(length=20)

    if not drugs:
        cursor = db[DRUG_AVAILABILITY].find(
            {
                "condition": condition_key,
                "state": state.lower()
            },
            {"_id": 0}
        )
        drugs = await cursor.to_list(length=20)

    available     = [d for d in drugs if d.get("available")]
    unavailable   = [d for d in drugs if not d.get("available")]
    alternatives  = []

    for drug in unavailable:
        if drug.get("alternative"):
            alt = await db[DRUG_AVAILABILITY].find_one(
                {
                    "drug_name": drug["alternative"],
                    "district":  district.lower()
                },
                {"_id": 0}
            )
            if alt:
                alternatives.append(alt)

    nearest_phc = drugs[0].get("nearest_phc") if drugs else None

    return {
        "available_drugs":   available,
        "unavailable_drugs": unavailable,
        "alternatives":      alternatives,
        "nearest_phc":       nearest_phc,
        "district":          district,
        "state":             state
    }