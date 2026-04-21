# api/main.py
from fastapi import FastAPI
from api.routes import rag

app = FastAPI(title="SehatSaathi Backend")

app.include_router(rag.router)

@app.get("/health")
def health():
    return {"status": "ok"}