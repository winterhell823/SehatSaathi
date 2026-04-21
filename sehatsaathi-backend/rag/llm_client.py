import os
from groq import Groq
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