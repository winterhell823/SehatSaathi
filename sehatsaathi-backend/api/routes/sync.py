from fastapi import APIRouter, HTTPException
from models.schemas import (
    DiagnosisEvent, FLGradientEvent,
    SymptomStreamEvent, ReferralEvent
)
from kafka.producer import (
    publish_diagnosis, publish_fl_gradient,
    publish_symptom, publish_referral
)

router = APIRouter()

@router.post("/diagnosis", status_code=202)
async def sync_diagnosis(event: DiagnosisEvent):
    try:
        publish_diagnosis(event)
        return {"status": "queued", "topic": "diagnosis-events", "patient_id": event.patient_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/fl-gradient", status_code=202)
async def sync_fl_gradient(event: FLGradientEvent):
    try:
        publish_fl_gradient(event)
        return {"status": "queued", "topic": "fl-gradients", "device_id": event.device_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/symptom", status_code=202)
async def sync_symptom(event: SymptomStreamEvent):
    try:
        publish_symptom(event)
        return {"status": "queued", "topic": "symptom-stream"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/referral", status_code=202)
async def sync_referral(event: ReferralEvent):
    try:
        publish_referral(event)
        return {"status": "queued", "topic": "referral-events", "patient_id": event.patient_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))