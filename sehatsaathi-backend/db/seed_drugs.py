from pymongo import MongoClient
import os

DRUG_DATA = [
    {"district": "varanasi",   "state": "UP",          "drug_name": "permethrin_cream",      "condition": "scabies",         "available": True,  "alternative": "benzyl_benzoate",      "nearest_phc": "Varanasi PHC Block 3"},
    {"district": "varanasi",   "state": "UP",          "drug_name": "benzyl_benzoate",       "condition": "scabies",         "available": True,  "alternative": None,                   "nearest_phc": "Varanasi PHC Block 3"},
    {"district": "gaya",       "state": "Bihar",       "drug_name": "chloroquine",           "condition": "malaria",         "available": True,  "alternative": "artemisinin",          "nearest_phc": "Gaya District Hospital"},
    {"district": "gaya",       "state": "Bihar",       "drug_name": "artemisinin",           "condition": "malaria",         "available": False, "alternative": "chloroquine",          "nearest_phc": "Bodh Gaya CHC"},
    {"district": "nashik",     "state": "Maharashtra", "drug_name": "clotrimazole",          "condition": "ringworm",        "available": True,  "alternative": "fluconazole",          "nearest_phc": "Nashik Rural PHC"},
    {"district": "nashik",     "state": "Maharashtra", "drug_name": "ciprofloxacin",         "condition": "wound_infection", "available": True,  "alternative": "amoxicillin",          "nearest_phc": "Nashik Rural PHC"},
    {"district": "coimbatore", "state": "Tamil Nadu",  "drug_name": "ofloxacin_drops",       "condition": "conjunctivitis",  "available": False, "alternative": "chloramphenicol_drops","nearest_phc": "Coimbatore Block PHC"},
    {"district": "coimbatore", "state": "Tamil Nadu",  "drug_name": "chloramphenicol_drops", "condition": "conjunctivitis",  "available": True,  "alternative": None,                   "nearest_phc": "Coimbatore Block PHC"},
]

def seed():
    uri = os.getenv("MONGO_URI", "mongodb://localhost:27017")
    client = MongoClient(uri)
    db = client[os.getenv("MONGO_DB_NAME", "vitalai")]
    collection = db["drug_availability"]
    collection.delete_many({})
    result = collection.insert_many(DRUG_DATA)
    print(f"Seeded {len(result.inserted_ids)} drug availability records")
    client.close()

if __name__ == "__main__":
    seed()