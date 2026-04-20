from fastapi import APIRouter, HTTPException
from models.schemas import RAGQueryRequest, TreatmentPlanResponse
from db.mongo_client import get_db
from db.collections import DRUG_AVAILABILITY

router = APIRouter()

@router.post("/query", response_model=TreatmentPlanResponse)
async def rag_query(request: RAGQueryRequest):
    try:
        drugs = await get_drug_availability(request.diagnosis, request.district)
        return TreatmentPlanResponse(
            diagnosis=request.diagnosis,
            treatment_steps=[
                "Confirm diagnosis visually",
                "Check patient allergy history",
                "Administer first-line treatment",
                "Schedule follow-up in 7 days"
            ],
            drugs=drugs,
            referral_recommended=request.confidence < 0.7,
            referral_reason="Low confidence — recommend clinical confirmation" if request.confidence < 0.7 else None,
            nearest_facility=drugs[0].get("nearest_phc") if drugs else None
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

async def get_drug_availability(diagnosis: str, district: str) -> list:
    db = get_db()
    cursor = db[DRUG_AVAILABILITY].find(
        {
            "condition": diagnosis.lower().replace(" ", "_"),
            "district":  district.lower()
        },
        {"_id": 0}
    )
    return await cursor.to_list(length=10)