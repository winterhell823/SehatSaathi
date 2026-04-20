import asyncio
import os
from db.mongo_client import connect_db, get_db
from db.collections import KNOWLEDGE_CHUNKS
from rag.embedder import encode_batch

KNOWLEDGE_BASE = [
    {"source": "WHO_guidelines", "condition": "scabies",         "text": "Scabies treatment: Apply permethrin 5% cream to entire body from neck down. Leave for 8-12 hours then wash off. Treat all household contacts simultaneously. Wash all clothing and bedding in hot water."},
    {"source": "WHO_guidelines", "condition": "scabies",         "text": "Alternative scabies treatment: Benzyl benzoate 25% lotion applied to whole body. Repeat after 24 hours. Oral ivermectin 200 mcg/kg for crusted scabies or when topical treatment fails."},
    {"source": "WHO_guidelines", "condition": "conjunctivitis",  "text": "Bacterial conjunctivitis: Chloramphenicol 0.5% eye drops every 2 hours for first 48 hours then every 4 hours. Continue for 5-7 days. Clean eye with clean cloth soaked in warm water."},
    {"source": "WHO_guidelines", "condition": "conjunctivitis",  "text": "Refer conjunctivitis immediately if: vision is affected, eye is very painful, cornea appears cloudy, or no improvement after 48 hours of treatment."},
    {"source": "WHO_guidelines", "condition": "wound_infection", "text": "Wound infection management: Clean wound with saline or clean water. Apply antiseptic. For mild infection: amoxicillin 500mg three times daily for 7 days. For severe infection or spreading redness: refer to hospital immediately."},
    {"source": "WHO_guidelines", "condition": "wound_infection", "text": "Signs requiring emergency referral: red streaks spreading from wound, high fever above 38.5 degrees, pus with foul smell, wound not healing after 7 days, patient is diabetic or immunocompromised."},
    {"source": "WHO_guidelines", "condition": "ringworm",        "text": "Ringworm (tinea) treatment: Apply clotrimazole 1% cream to affected area twice daily. Continue for 2 weeks after symptoms resolve. Keep area clean and dry. Do not share towels or clothing."},
    {"source": "WHO_guidelines", "condition": "malaria",         "text": "Uncomplicated malaria treatment: Artemisinin-based combination therapy (ACT) is first line. Artemether-lumefantrine taken with food twice daily for 3 days. Check local resistance patterns."},
    {"source": "WHO_guidelines", "condition": "malaria",         "text": "Severe malaria indicators requiring immediate referral: altered consciousness, convulsions, difficulty breathing, jaundice, haemoglobin below 7, inability to stand."},
    {"source": "WHO_guidelines", "condition": "jaundice",        "text": "Jaundice in adults: Check for hepatitis A, B. Advise rest and adequate hydration. Avoid alcohol and paracetamol. Refer if: fever above 38, confusion, bleeding, very dark urine with pale stools, jaundice worsening after 2 weeks."},
    {"source": "AIIMS_rural",    "condition": "general",         "text": "Referral decision guide for rural CHWs: Refer immediately if patient has difficulty breathing, chest pain, loss of consciousness, severe dehydration, high fever above 40 degrees, or if CHW is unsure about diagnosis."},
    {"source": "AIIMS_rural",    "condition": "general",         "text": "Follow-up schedule: Mild conditions reviewed in 7 days. Moderate conditions reviewed in 3 days. If patient worsens before scheduled review, instruct them to return immediately."},
]

async def load_knowledge():
    await connect_db()
    db = get_db()

    existing = await db[KNOWLEDGE_CHUNKS].count_documents({})
    if existing > 0:
        print(f"[knowledge_loader] {existing} chunks already loaded. Skipping.")
        return

    print