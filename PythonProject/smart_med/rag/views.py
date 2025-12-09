# rag/views.py
import time
import traceback

from rest_framework.views import APIView
from rest_framework.response import Response

from .services import retrieve_top_chunks, build_answer
from .tasks import run_rag_task
from .utils import (
    is_medical_question,
    get_non_medical_response,
    is_greeting,
    get_greeting_response,
)
from celery.result import AsyncResult


class DrugRAGView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        question = request.data.get("question")
        mode = request.data.get("mode", "async")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        # 1) 인사말 즉시 처리
        if is_greeting(question):
            return Response({
                "status": "done",
                "question": question,
                "result": {
                    "answer": get_greeting_response(),
                    "contexts": [],
                },
            })

        # 2) 의료 질문 체크
        if not is_medical_question(question):
            return Response({
                "status": "rejected",
                "question": question,
                "result": {
                    "answer": get_non_medical_response(),
                    "contexts": [],
                },
            })

        # 3) 비동기 처리 (Celery)
        if mode == "async":
            task = run_rag_task.delay(question)
            return Response({"task_id": task.id, "status": "processing"}, status=202)

        # 4) 동기 처리
        try:
            start = time.time()
            chunks = retrieve_top_chunks(question, k=5)
            answer = build_answer(question, chunks)
            elapsed = time.time() - start
            print(f"[SYNC-RAG] elapsed={elapsed:.2f}s")
        except Exception as e:
            traceback.print_exc()
            return Response({"detail": "RAG 처리 오류", "error": str(e)}, status=500)

        # context fallback
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

        return Response({
            "status": "done",
            "question": question,
            "result": {
                "answer": answer,
                "contexts": contexts,
            },
        })


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
            data = result.result
            return Response({
                "status": "done",
                "question": data.get("question"),
                "result": data.get("result")
            })

        return Response({"status": result.state})
