# rag/retriever.py
# PURPOSE: Given a patient's symptoms, fetch the most relevant medical 
# knowledge chunks from MongoDB using vector similarity search.
# This is the "R" in RAG (Retrieval-Augmented Generation).

from db.mongo_client import get_db
from rag.embedder import embed_text

# MongoDB collection where your medical knowledge is stored as vectors
KNOWLEDGE_COLLECTION = "medical_knowledge"

def retrieve_relevant_context(query: str, top_k: int = 5) -> list[dict]:
    """
    Takes a symptom description and finds the top_k most relevant
    medical knowledge chunks from MongoDB.
    
    Uses MongoDB Atlas Vector Search (or $vectorSearch aggregation stage).
    
    Returns: list of dicts with 'text' and 'score' keys
    """
    db = get_db()
    collection = db[KNOWLEDGE_COLLECTION]
    
    # Step 1: Convert the query to a vector
    query_vector = embed_text(query)
    
    # Step 2: Run MongoDB vector similarity search
    # This uses cosine similarity to find closest knowledge chunks
    pipeline = [
        {
            "$vectorSearch": {
                "index": "medical_knowledge_vector_index",  # Atlas Search index name
                "path": "embedding",                         # Field storing the vector
                "queryVector": query_vector,
                "numCandidates": top_k * 10,                # Search wider, return fewer
                "limit": top_k
            }
        },
        {
            "$project": {
                "text": 1,
                "source": 1,          # e.g. "WHO guidelines", "ICMR protocol"
                "disease": 1,         # e.g. "Tuberculosis"
                "score": {"$meta": "vectorSearchScore"},
                "_id": 0
            }
        }
    ]
    
    results = list(collection.aggregate(pipeline))
    return results

def format_context_for_prompt(results: list[dict]) -> str:
    """
    Converts retrieved chunks into a clean string to inject into the LLM prompt.
    The LLM will use this as its reference knowledge.
    """
    if not results:
        return "No relevant medical context found."
    
    context_parts = []
    for i, result in enumerate(results, 1):
        source = result.get("source", "Medical Reference")
        text   = result.get("text", "")
        context_parts.append(f"[{i}] Source: {source}\n{text}")
    
    return "\n\n".join(context_parts)