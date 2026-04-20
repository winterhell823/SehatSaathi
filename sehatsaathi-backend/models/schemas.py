from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime

class DiagnosisEvent(BaseModel):
    patient_id: str
    chw_id: str
    village: str
    district: str
    state: str
    lat: float
    lng: float
    symptoms: List[str]
    image_score: float
    top_diagnosis: str
    all_diagnoses: List[dict]
    confidence: float
    trust_level: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)

class FLGradientEvent(BaseModel):
    device_id: str
    model_version: str
    gradient_payload: str
    num_samples: int
    timestamp: datetime = Field(default_factory=datetime.utcnow)

class SymptomStreamEvent(BaseModel):
    chw_id: str
    village: str
    district: str
    state: str
    lat: float
    lng: float
    symptoms: List[str]
    timestamp: datetime = Field(default_factory=datetime.utcnow)

class ReferralEvent(BaseModel):
    patient_id: str
    chw_id: str
    diagnosis: str
    confidence: float
    referral_type: str
    nearest_facility: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)

class RAGQueryRequest(BaseModel):
    patient_id: str
    diagnosis: str
    confidence: float
    symptoms: List[str]
    district: str
    state: str

class TreatmentPlanResponse(BaseModel):
    diagnosis: str
    treatment_steps: List[str]
    drugs: List[dict]
    referral_recommended: bool
    referral_reason: Optional[str]
    nearest_facility: Optional[str]