# rag/tasks.py
from celery import shared_task

from .services import retrieve_top_chunks, build_answer


@shared_task(bind=True)
def run_rag_task(self, question):
    try:
        # 1) RAG 검색
        chunks = retrieve_top_chunks(question, k=5)
        answer = build_answer(question, chunks)

        # 2) context fallback
        if not chunks:
            contexts = [{
                "chunk_id": "",
                "item_name": "",
                "section": "",
                "chunk_index": 0
            }]
        else:
            contexts = [{
                "chunk_id": c.chunk_id,
                "item_name": c.item_name,
                "section": c.section,
                "chunk_index": c.chunk_index,
            } for c in chunks]

        return {
            "status": "done",
            "question": question,
            "result": {
                "answer": answer,
                "contexts": contexts,
            }
        }

    except Exception as e:
        return {"status": "failed", "error": str(e)}
