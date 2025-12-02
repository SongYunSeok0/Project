# rag/views.py
import time

from rest_framework.views import APIView
from rest_framework.response import Response

from .services import retrieve_top_chunks, build_answer
from .intent_detector import detect_intent


class DrugRAGView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        start = time.time()
        question = request.data.get("question")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        try:
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