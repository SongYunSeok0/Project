# rag/views.py
import time
from rest_framework.views import APIView
from rest_framework.response import Response

from .services import retrieve_top_chunks, build_answer
from .tasks import run_rag_task
from celery.result import AsyncResult


class DrugRAGView(APIView):
    """
    약학 RAG 질의 처리 View
    - mode=async  : Celery 비동기 처리
    - mode=sync   : 기존 방식(즉시 응답)
    """
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        question = request.data.get("question")
        mode = request.data.get("mode", "async")  # default = async

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        # ---------------------------
        # 🔹 1. 비동기 모드 (Celery)
        # ---------------------------
        if mode == "async":
            task = run_rag_task.delay(question)
            return Response(
                {"task_id": task.id, "status": "processing"},
                status=202,
            )

        # ---------------------------
        # 🔹 2. 동기 모드 (즉시 RAG 처리)
        # ---------------------------
        try:
            start = time.time()
            chunks = retrieve_top_chunks(question, k=5)
            answer = build_answer(question, chunks)

            elapsed = time.time() - start
            print(f"[SYNC-RAG] q='{question[:30]}' elapsed={elapsed:.2f}s")

        except Exception as e:
            return Response(
                {"detail": "RAG 처리 중 오류", "error": str(e)},
                status=500
            )

        # 응답 형식 Celery와 동일하게 통일
        return Response(
            {
                "status": "done",
                "result": {
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
            },
            status=200
        )


class RAGTaskResultView(APIView):
    """
    Celery Task 결과 조회 API
    GET /rag/result/<task_id>/
    """
    authentication_classes = []
    permission_classes = []

    def get(self, request, task_id):
        result = AsyncResult(task_id)

        if result.state in ["PENDING", "RECEIVED"]:
            return Response({"status": "pending"})

        if result.state == "STARTED":
            return Response({"status": "processing"})

        if result.state == "FAILURE":
            return Response({"status": "failed", "error": str(result.result)})

        if result.state == "SUCCESS":
            return Response(
                {
                    "status": "done",
                    "result": result.result
                }
            )

        # 예외적인 상태
        return Response({"status": result.state}, status=200)
