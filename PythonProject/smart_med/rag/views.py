# rag/views.py
from rest_framework.views import APIView
from rest_framework.response import Response
from celery.result import AsyncResult
import time

from .services import retrieve_top_chunks, build_answer
from .tasks import run_rag_task
from .docs import rag_query_docs, rag_result_docs


@rag_query_docs
class DrugRAGView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        question = request.data.get("question")
        mode = request.data.get("mode", "async")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        # --- 비동기 ---
        if mode == "async":
            task = run_rag_task.delay(question)
            return Response({"task_id": task.id, "status": "processing"}, status=202)

        # --- 동기 ---
        try:
            start = time.time()
            chunks = retrieve_top_chunks(question, k=5)
            answer = build_answer(question, chunks)
            print(f"[SYNC-RAG] q='{question[:30]}' elapsed={time.time() - start:.2f}s")

        except Exception as e:
            return Response({"detail": "RAG 처리 중 오류", "error": str(e)}, status=500)

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
                    ]
                },
            }
        )


@rag_result_docs
class RAGTaskResultView(APIView):
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
            return Response({"status": "done", "result": result.result})

        return Response({"status": result.state})
