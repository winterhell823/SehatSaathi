from db.mongo_client import get_db
from db.collections import KNOWLEDGE_CHUNKS
from rag.embedder import encode

TOP_K = int(__import__('os').getenv("RAG_TOP_K", "5"))

async def retrieve_context(query: str) -> list[str]:
    db = get_db()
    query_embedding = encode(query)

    pipeline = [
        {
            "$vectorSearch": {
                "index":       "knowledge_vector_index",
                "path":        "embedding",
                "queryVector": query_embedding,
                "numCandidates": TOP_K * 10,
                "limit":       TOP_K
            }
        },
        {
            "$project": {
                "_id":   0,
                "text":  1,
                "source": 1,
                "score": {"$meta": "vectorSearchScore"}
            }
        }
    ]

    cursor = db[KNOWLEDGE_CHUNKS].aggregate(pipeline)
    results = await cursor.to_list(length=TOP_K)

    chunks = [r["text"] for r in results]
    print(f"[retriever] Retrieved {len(chunks)} chunks for query: {query[:60]}...")
    return chunks