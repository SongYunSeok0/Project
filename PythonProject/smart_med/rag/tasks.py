from celery import shared_task
from .services import retrieve_top_chunks, build_answer

@shared_task
def run_rag_task(question: str) -> dict:
    try:
        chunks = retrieve_top_chunks(question, k=5)
        answer = build_answer(question, chunks)

        return {
            "answer": answer,
            "contexts": [
                {
                    "chunk_id": c.chunk_id,
                    "item_name": c.item_name,
                    "section": c.section,
                    "chunk_index": c.chunk_index,
                }
                for c in chunks
            ],
        }

    except Exception as e:
        return {"error": str(e)}
