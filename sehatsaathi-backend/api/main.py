# PURPOSE: Simple health check endpoint to verify the server is running.
from fastapi import FastAPI
from contextlib import asynccontextmanager
from api.routes import sync, model, rag
from db.mongo_client import connect_db, close_db
from db.indexes import create_indexes
from kafka.producer import init_producer, close_producer

@asynccontextmanager
async def lifespan(app: FastAPI):
    await connect_db()
    await create_indexes()
    init_producer()
    yield
    await close_db()
    close_producer()

app = FastAPI(title="VitalAI Backend", version="1.0.0", lifespan=lifespan)

app.include_router(sync.router,  prefix="/sync",  tags=["Sync"])
app.include_router(model.router, prefix="/model", tags=["Model"])
app.include_router(rag.router,   prefix="/rag",   tags=["RAG"])

@app.get("/health")
async def health():
    return {"status": "ok"}