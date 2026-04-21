# rag/treatment_mapper.py
# PURPOSE: Core RAG pipeline
# Flow: receive symptoms+image → initial prediction → ask max 5 followups 
#       → final comprehensive evaluation → save everything to MongoDB

import json, re, base64
from rag.llm_client import call_groq
from db.mongo_client import get_db
from datetime import datetime

SYSTEM_PROMPT = """You are SehatSaathi, an expert AI medical assistant for rural India, assisting ASHA workers and rural doctors.

YOUR FLOW:
Step 1 — When user first describes symptoms (with or without image):
  - Analyze everything provided
  - Identify the SINGLE most probable disease immediately
  - Then ask the FIRST follow-up question to confirm or refine your prediction

Step 2 — Ask follow-up questions one at a time (maximum 5 total):
  - Each question must be simple, conversational, in plain language
  - Each question should help confirm or rule out the predicted disease
  - After each answer, update your internal confidence

Step 3 — After all follow-up answers (or when confident enough before 5):
  - Give a complete, detailed final evaluation

RESPONSE FORMAT — return ONLY one of these three JSON formats, no plain text ever:

FORMAT A — Initial prediction + first follow-up question:
{
  "status": "initial",
  "predicted_disease": "name of most probable disease",
  "initial_confidence": "high/medium/low",
  "message": "Brief friendly message acknowledging symptoms and stating initial prediction",
  "follow_up_question": "First specific question to ask",
  "question_number": 1
}

FORMAT B — Continuing follow-up questions (questions 2 to 5):
{
  "status": "follow_up",
  "question_number": 2,
  "message": "follow-up question here"
}

FORMAT C — Final evaluation (after enough answers OR after question 5):
{
  "status": "diagnosed",
  "message": "Warm, clear summary message for the patient/doctor",
  "condition": {
    "name": "Disease name",
    "confidence_percentage": ,
    "reasoning": "Detailed explanation of why this diagnosis — connecting symptoms and follow-up answers",
    "differential_diagnoses": ["Other possibility 1", "Other possibility 2"]
  },
  "further_predictions": "What may happen if untreated or how disease may progress",
  "precautions": ["precaution 1", "precaution 2", "precaution 3"],
  "recommended_treatment": "Detailed first-line treatment description",
  "medications": [
    {"name": "...", "dose": "...", "duration": "...", "instructions": "..."}
  ],
  "next_steps": ["Immediate step 1", "Step 2", "Step 3"],
  "referral_needed": true/false,
  "referral_reason": "Why referral is needed if applicable",
  "red_flags": ["Warning sign to watch for 1", "Warning sign 2"],
  "follow_up_days": 3
}

STRICT RULES:
- Always return valid JSON only — no text outside the JSON block
- Only ONE primary condition in final diagnosis
- question_number must always increment correctly
- After status diagnosed, conversation is permanently closed
- Base all medical advice on ICMR and WHO India guidelines"""


def build_messages(conversation_history: list, new_user_message: str = None,
                   image_base64: str = None, image_media_type: str = "image/jpeg") -> list:
    """
    Assembles full message list for Groq.
    Includes system prompt + all previous turns + new user message.
    Supports text, image, or both.
    """
    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(conversation_history)

    if new_user_message or image_base64:
        if image_base64:
            # Image + text together — Groq vision model can read both
            content = [
                {
                    "type": "image_url",
                    "image_url": {
                        "url": f"data:{image_media_type};base64,{image_base64}"
                    }
                },
                {
                    "type": "text",
                    "text": new_user_message 
                }
            ]
        else:
            content = new_user_message

        messages.append({"role": "user", "content": content})

    return messages


def parse_response(raw: str) -> dict:
    """Safely parses JSON from Groq response."""
    try:
        match = re.search(r'\{.*\}', raw, re.DOTALL)
        if match:
            return json.loads(match.group())
    except (json.JSONDecodeError, AttributeError):
        pass
    # Fallback if parsing fails
    return {
        "status": "diagnosed",
        "message": "Unable to process response. Please consult a doctor.",
        "condition": {
            "name": "Unknown",
            "confidence_percentage": 0,
            "reasoning": raw,
            "differential_diagnoses": []
        },
        "further_predictions": "Not available",
        "precautions": [],
        "recommended_treatment": "Please refer to a doctor immediately",
        "medications": [],
        "next_steps": ["Visit nearest PHC or hospital"],
        "referral_needed": True,
        "referral_reason": "Could not complete diagnosis",
        "red_flags": [],
        "follow_up_days": 1
    }


def extract_full_symptom_summary(conversation_history: list, initial_message: str) -> dict:
    """
    Extracts and structures everything said during the conversation
    to save as patient history in MongoDB.

    Returns a clean summary of symptoms + all follow-up answers.
    """
    symptoms_text = initial_message
    followup_qa = []

    # Walk through history to collect all Q&A pairs
    i = 0
    while i < len(conversation_history):
        turn = conversation_history[i]

        if turn["role"] == "assistant":
            try:
                data = json.loads(turn["content"]) if isinstance(turn["content"], str) else turn["content"]
                status = data.get("status")

                # Collect follow-up questions and their answers
                if status in ("initial", "follow_up"):
                    question = data.get("follow_up_question") or data.get("message")
                    # Get the next user turn as the answer
                    if i + 1 < len(conversation_history) and conversation_history[i + 1]["role"] == "user":
                        answer = conversation_history[i + 1]["content"]
                        followup_qa.append({
                            "question_number": data.get("question_number"),
                            "question": question,
                            "answer": answer
                        })
            except (json.JSONDecodeError, TypeError):
                pass
        i += 1

    return {
        "initial_symptoms": symptoms_text,
        "followup_answers": followup_qa
    }


def save_to_patient_history(
    patient_id: str,
    initial_message: str,
    conversation_history: list,
    final_diagnosis: dict,
    has_image: bool = False
):
    """
    Saves the complete consultation to MongoDB patient history.
    Called only when final diagnosis is reached.

    Saves:
    - Initial symptoms (text)
    - All follow-up Q&A
    - Final diagnosis with all details
    - Timestamp
    - Whether image was provided
    """
    db = get_db()
    collection = db["patient_encounters"]

    symptom_summary = extract_full_symptom_summary(conversation_history, initial_message)

    record = {
        "patient_id": patient_id,
        "timestamp": datetime.utcnow(),
        "has_image": has_image,

        # Everything the patient/doctor said
        "symptoms": symptom_summary["initial_symptoms"],
        "followup_qa": symptom_summary["followup_answers"],

        # Final diagnosis details
        "diagnosis": {
            "condition": final_diagnosis.get("condition", {}),
            "confidence_percentage": final_diagnosis.get("condition", {}).get("confidence_percentage"),
            "further_predictions": final_diagnosis.get("further_predictions"),
            "precautions": final_diagnosis.get("precautions", []),
            "recommended_treatment": final_diagnosis.get("recommended_treatment"),
            "medications": final_diagnosis.get("medications", []),
            "next_steps": final_diagnosis.get("next_steps", []),
            "referral_needed": final_diagnosis.get("referral_needed"),
            "referral_reason": final_diagnosis.get("referral_reason"),
            "red_flags": final_diagnosis.get("red_flags", []),
            "follow_up_days": final_diagnosis.get("follow_up_days")
        }
    }

    collection.insert_one(record)


def get_diagnosis(
    conversation_history: list,
    new_user_message: str = None,
    image_base64: str = None,
    image_media_type: str = "image/jpeg",
    patient_id: str = "anonymous",
    initial_message: str = None    # original first message, passed every time for saving
) -> dict:
    """
    Main entry point called by API route on every chat turn.

    conversation_history: [] on first call, grows each turn
    new_user_message:     what user just typed
    image_base64:         optional, only on first message
    patient_id:           to save history under correct patient
    initial_message:      the very first user message, passed along for record saving
    """
    messages = build_messages(
        conversation_history=conversation_history,
        new_user_message=new_user_message,
        image_base64=image_base64,
        image_media_type=image_media_type
    )

    raw = call_groq(messages=messages)
    result = parse_response(raw)

    # When diagnosis is final, save everything to MongoDB
    if result.get("status") == "diagnosed":
        save_to_patient_history(
            patient_id=patient_id,
            initial_message=initial_message or new_user_message or "",
            conversation_history=conversation_history,
            final_diagnosis=result,
            has_image=image_base64 is not None
        )

    return result

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
