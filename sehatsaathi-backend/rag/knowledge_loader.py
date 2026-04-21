# rag/knowledge_loader.py
# PURPOSE: One-time setup script to load medical knowledge (WHO guidelines, 
# ICMR protocols, drug info) into MongoDB with vector embeddings.
# Run this once, or whenever you add new knowledge documents.

import json
from db.mongo_client import get_db
from rag.embedder import embed_batch

KNOWLEDGE_COLLECTION = "medical_knowledge"

def chunk_text(text: str, chunk_size: int = 500, overlap: int = 50) -> list[str]:
    """
    Splits long documents into smaller overlapping chunks.
    Why: LLMs and vector search work better on focused chunks than huge documents.
    overlap: Each chunk shares 50 chars with the next to preserve context at boundaries.
    """
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunks.append(text[start:end])
        start += chunk_size - overlap
    return chunks

def load_knowledge_from_json(filepath: str):
    """
    Loads knowledge from a JSON file and stores in MongoDB with embeddings.
    
    Expected JSON format:
    [
      {
        "disease": "Tuberculosis",
        "source": "ICMR Guidelines 2023",
        "content": "Full guideline text here..."
      },
      ...
    ]
    """
    db = get_db()
    collection = db[KNOWLEDGE_COLLECTION]
    
    with open(filepath, "r") as f:
        documents = json.load(f)
    
    records_to_insert = []
    
    for doc in documents:
        # Split long content into chunks
        chunks = chunk_text(doc["content"])
        
        # Embed all chunks at once (efficient batch operation)
        embeddings = embed_batch(chunks)
        
        for chunk_text_content, embedding in zip(chunks, embeddings):
            records_to_insert.append({
                "disease":   doc.get("disease", "General"),
                "source":    doc.get("source", "Unknown"),
                "text":      chunk_text_content,
                "embedding": embedding    # 384-dimensional vector
            })
    
    # Insert all at once
    if records_to_insert:
        collection.insert_many(records_to_insert)
        print(f"Inserted {len(records_to_insert)} knowledge chunks into MongoDB")

def create_vector_index():
    """
    Creates the MongoDB Atlas Vector Search index on the embedding field.
    Run this once after loading data.
    You can also create this manually in MongoDB Atlas UI.
    """
    db = get_db()
    # Atlas Search index definition — run via Atlas UI or mongocli
    index_definition = {
        "name": "medical_knowledge_vector_index",
        "type": "vectorSearch",
        "definition": {
            "fields": [{
                "type": "vector",
                "path": "embedding",
                "numDimensions": 384,        # Must match your embedding model output
                "similarity": "cosine"
            }]
        }
    }
    print("Create this index in MongoDB Atlas UI:", json.dumps(index_definition, indent=2))

if __name__ == "__main__":
    load_knowledge_from_json("data/medical_knowledge.json")
    create_vector_index()