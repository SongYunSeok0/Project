from celery import shared_task
from .services import (
    retrieve_top_chunks,
    build_answer,
    detect_intent,
    INTENT_COMPARE,
    detect_compare_intent,
    _select_chunk_for_med,
)

@shared_task
def run_rag_task(question: str) -> dict:
    """
    Celery 백그라운드에서 실행되는 RAG Task
    """
    try:
        intent = detect_intent(question)

        if intent == INTENT_COMPARE:
            meds = detect_compare_intent(question) or []
            compare_chunks = []
            for m in meds:
                for sec in ["효능효과", "부작용", "용법용량", "주의"]:
                    c = _select_chunk_for_med(m, [sec])
                    if c and c not in compare_chunks:
                        compare_chunks.append(c)

            answer = build_answer(question, compare_chunks)
            chunks = compare_chunks

        else:
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
