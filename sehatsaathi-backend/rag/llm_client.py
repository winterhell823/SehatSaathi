
import os
from groq import Groq

import httpx

from dotenv import load_dotenv

load_dotenv()


# Initialize the Groq client using your API key from .env
client = Groq(api_key=os.environ.get("GROQ_API_KEY"))

# The model to use — llama3-70b is powerful and fast on Groq
GROQ_MODEL = "llama-3.1-8b-instant"

def call_groq(messages: list, max_tokens: int = 1024) -> str:
    """
    Sends full messages list to Groq and returns text response.
    messages: complete list including system prompt and history
              built by treatment_mapper.build_messages()
    """
    response = client.chat.completions.create(
        model=GROQ_MODEL,
        messages=messages,
        max_tokens=max_tokens,
        temperature=0.2
    )
    return response.choices[0].message.content


LLM_PROVIDER = os.getenv("LLM_PROVIDER", "huggingface")
OLLAMA_HOST  = os.getenv("OLLAMA_HOST", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "biomistral")
HF_TOKEN     = os.getenv("HF_API_TOKEN", "")
HF_MODEL     = os.getenv("HF_MODEL", "BioMistral/BioMistral-7B")

SYSTEM_PROMPT = """You are VitalAI, a medical assistant helping rural healthcare workers in India.
Given a patient diagnosis, symptoms, and retrieved medical context, provide:
1. A clear step-by-step treatment plan
2. First-line drugs to administer
3. Whether to refer the patient and why
Keep responses simple, practical, and suitable for a non-doctor healthcare worker."""

async def call_llm(diagnosis: str, symptoms: list[str], context_chunks: list[str]) -> str:
    context = "\n\n".join(context_chunks) if context_chunks else "No additional context available."

    prompt = f"""{SYSTEM_PROMPT}

Retrieved medical context:
{context}

Patient diagnosis: {diagnosis}
Reported symptoms: {", ".join(symptoms)}

Treatment plan:"""

    if LLM_PROVIDER == "ollama":
        return await _call_ollama(prompt)
    return await _call_huggingface(prompt)

async def _call_ollama(prompt: str) -> str:
    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(
            f"{OLLAMA_HOST}/api/generate",
            json={
                "model":  OLLAMA_MODEL,
                "prompt": prompt,
                "stream": False,
                "options": {
                    "temperature": 0.2,
                    "top_p":       0.9,
                    "num_predict": 512
                }
            }
        )
        response.raise_for_status()
        return response.json()["response"]

async def _call_huggingface(prompt: str) -> str:
    async with httpx.AsyncClient(timeout=60.0) as client:
        response = await client.post(
            f"https://api-inference.huggingface.co/models/{HF_MODEL}",
            headers={"Authorization": f"Bearer {HF_TOKEN}"},
            json={
                "inputs": prompt,
                "parameters": {
                    "max_new_tokens":  512,
                    "temperature":     0.2,
                    "return_full_text": False
                }
            }
        )
        response.raise_for_status()
        result = response.json()
        if isinstance(result, list):
            return result[0]["generated_text"]
        return result.get("generated_text", "")
