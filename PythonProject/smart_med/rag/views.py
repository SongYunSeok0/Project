# rag/views.py
import time

from rest_framework.views import APIView
from rest_framework.response import Response

from .services import (
    retrieve_top_chunks,
    build_answer,
    detect_intent,
    INTENT_COMPARE,
    detect_compare_intent,
    _select_chunk_for_med,   # 필요하면 public 으로 빼기
)


class DrugRAGView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        start = time.time()
        question = request.data.get("question")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        try:
            intent = detect_intent(question)

            if intent == INTENT_COMPARE:
                meds = detect_compare_intent(question) or []
                # 두 약에 대해 대표 chunk 몇 개씩 contexts로 제공
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

        except Exception as e:
            elapsed = time.time() - start
            print(f"[RAG][ERROR] q='{question[:30]}' elapsed={elapsed:.2f}s -> {repr(e)}")
            return Response(
                {"detail": "RAG 처리 중 오류", "error": str(e)},
                status=500,
            )

        print("[RAG-ANSWER]", repr(answer), "LEN=", len(answer))
        elapsed = time.time() - start
        print(f"[RAG] q='{question[:30]}' elapsed={elapsed:.2f}s")

        return Response(
            {
                "question": question,
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
            },
            status=200,
        )
