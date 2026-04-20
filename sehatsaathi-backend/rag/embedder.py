from sentence_transformers import SentenceTransformer
import numpy as np
import os

MODEL_NAME = os.getenv("EMBEDDING_MODEL", "pritamdeka/BioBERT-mnli-snli-scinli-scitail-mednli-stsb")

_model: SentenceTransformer = None

def get_embedder() -> SentenceTransformer:
    global _model
    if _model is None:
        print(f"[embedder] Loading embedding model: {MODEL_NAME}")
        _model = SentenceTransformer(MODEL_NAME)
    return _model

def encode(text: str) -> list[float]:
    model = get_embedder()
    embedding = model.encode(text, normalize_embeddings=True)
    return embedding.tolist()

def encode_batch(texts: list[str]) -> list[list[float]]:
    model = get_embedder()
    embeddings = model.encode(texts, normalize_embeddings=True, batch_size=32)
    return embeddings.tolist()