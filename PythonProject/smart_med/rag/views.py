from rest_framework.views import APIView
from rest_framework.response import Response
from .tasks import run_rag_task
from celery.result import AsyncResult
from django.conf import settings


class DrugRAGView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        question = request.data.get("question")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        # Celery Task 실행 (비동기)
        task = run_rag_task.delay(question)

        return Response(
            {
                "task_id": task.id,
                "status": "processing"
            },
            status=202,
        )

class RAGTaskResultView(APIView):
    authentication_classes = []
    permission_classes = []

    def get(self, request, task_id):
        result = AsyncResult(task_id)

        if result.state == "PENDING":
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