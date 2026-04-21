
# rag/embeddeer.py
# PURPOSE: Turns text (symptoms, medical knowledge) into numerical vectors.
# Vectors let you find "semantically similar" content even with different words.
# e.g. "chest pain" and "thoracic discomfort" should be close in vector spac.

from sentence_transformers import SentenceTransformer
import numpy as np

MODEL_NAME = "all-MiniLM-L6-v2"

# Load once at startup (expensive operation)
_model = None

def get_model() -> SentenceTransformer:
    global _model
    if _model is None:
        _model = SentenceTransformer(MODEL_NAME)
    return _model

def embed_text(text: str) -> list[float]:
    """
    Converts a single string to a vector.
    Used when a doctor submits symptoms for diagnosis.
    Returns a list of 384 floats.
    """
    model = get_model()
    vector = model.encode(text, normalize_embeddings=True)
    return vector.tolist()

def embed_batch(texts: list[str]) -> list[list[float]]:
    """
    Converts multiple strings to vectors at once.
    Used when loading the knowledge base into MongoDB.
    More efficient than calling embed_text() in a loop.
    """
    model = get_model()
    vectors = model.encode(texts, normalize_embeddings=True, batch_size=32)
    return vectors.tolist()


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
