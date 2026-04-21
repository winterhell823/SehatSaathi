# api/routes/rag.py
# PURPOSE: Chatbot endpoints — one message per call, stateless
# Android app maintains conversation_history and sends it each time

from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
import base64, json
from rag.treatment_mapper import get_diagnosis

router = APIRouter(prefix="/rag", tags=["Diagnosis Chatbot"])


class ChatRequest(BaseModel):
    message: str                       # what user just typed
    conversation_history: list = []    # all previous turns, [] on first message
    patient_id: str = ""      # to save history under correct patient
    initial_message: str = None        # very first message, pass this every turn for saving


def is_already_diagnosed(conversation_history: list) -> bool:
    """
    Checks conversation history for a diagnosed status.
    If found, chat is permanently closed — no more input accepted.
    """
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
        initial_message=request.initial_message or request.message
    )


@router.post("/chat-with-image")
async def chat_with_image(
    image: UploadFile = File(...),
    message: str = Form("Please analyze this image"),
    conversation_history: str = Form("[]"),
    patient_id: str = Form("anonymous"),
    initial_message: str = Form(None)
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
        initial_message=initial_message or message
    )