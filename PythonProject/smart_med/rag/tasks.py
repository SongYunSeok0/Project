# rag/tasks.py
from celery import shared_task

from .services import retrieve_top_chunks, build_answer, serialize_chunks
from .llm_loader import get_llm_model


@shared_task(bind=True)
def run_rag_task(self, question):
    try:
        # LLM 로딩
        tokenizer, model = get_llm_model()

        # 문서 검색
        chunks = retrieve_top_chunks(question, k=5)

        # 답변 생성
        answer = build_answer(question, chunks)

        return {
            "status": "done",
            "task_id": self.request.id,
            "result": {
                "answer": answer,
                "contexts": [
                    {
                        "chunk_id": c.chunk_id,
                        "item_name": c.item_name,
                        "section": c.section,
                        "chunk_index": c.chunk_index
                    }
                    for c in chunks
                ]
            }
        }


    except Exception as e:
        return {"status": "failed", "error": str(e)}


