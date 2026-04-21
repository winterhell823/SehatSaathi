from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
from models.schemas import RAGQueryRequest, TreatmentPlanResponse
from db.mongo_client import get_db
from db.collections import DRUG_AVAILABILITY
from rag.treatment_mapper import get_diagnosis

import base64
import json


router = APIRouter(tags=["RAG"])


class ChatRequest(BaseModel):
    message: str
    conversation_history: list = []
    patient_id: str = ""
    initial_message: str = None


def is_already_diagnosed(conversation_history: list) -> bool:
    for turn in conversation_history:
        if turn.get("role") == "assistant":
            try:
                data = json.loads(turn["content"]) if isinstance(turn["content"], str) else {}
                if data.get("status") == "diagnosed":
                    return True
            except (json.JSONDecodeError, TypeError):
                pass
    return False


@router.post("/chat")
async def chat(request: ChatRequest):
    if is_already_diagnosed(request.conversation_history):
        raise HTTPException(
            status_code=400,
            detail="Consultation is complete. Please start a new consultation."
        )

    return get_diagnosis(
        conversation_history=request.conversation_history,
        new_user_message=request.message,
        patient_id=request.patient_id,
        initial_message=request.initial_message or request.message,
    )


@router.post("/chat-with-image")
async def chat_with_image(
    image: UploadFile = File(...),
    message: str = Form("Please analyze this image"),
    conversation_history: str = Form("[]"),
    patient_id: str = Form("anonymous"),
    initial_message: str = Form(None),
):
    history = json.loads(conversation_history)

    if is_already_diagnosed(history):
        raise HTTPException(
            status_code=400,
            detail="Consultation is complete. Please start a new consultation."
        )

    image_bytes = await image.read()
    image_base64 = base64.b64encode(image_bytes).decode("utf-8")
    media_type = image.content_type or "image/jpeg"

    return get_diagnosis(
        conversation_history=history,
        new_user_message=message,
        image_base64=image_base64,
        image_media_type=media_type,
        patient_id=patient_id,
        initial_message=initial_message or message,
    )


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
                "Schedule follow-up in 7 days",
            ],
            drugs=drugs,
            referral_recommended=request.confidence < 0.7,
            referral_reason="Low confidence — recommend clinical confirmation" if request.confidence < 0.7 else None,
            nearest_facility=drugs[0].get("nearest_phc") if drugs else None,
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


async def get_drug_availability(diagnosis: str, district: str) -> list:
    db = get_db()
    cursor = db[DRUG_AVAILABILITY].find(
        {
            "condition": diagnosis.lower().replace(" ", "_"),
            "district": district.lower(),
        },
        {"_id": 0},
    )
    return await cursor.to_list(length=10)

