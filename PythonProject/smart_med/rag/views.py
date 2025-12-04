import time
from rest_framework.views import APIView
from rest_framework.response import Response
from .services import retrieve_top_chunks, build_answer
from .tasks import run_rag_task
from celery.result import AsyncResult
from drf_spectacular.utils import (
    extend_schema,
    OpenApiExample,
    OpenApiParameter,
    OpenApiResponse
)


@extend_schema(
    tags=["RAG"],
    summary="약학 RAG 질의 처리",
    description="""
약학 데이터 기반 RAG 검색 API입니다.
- mode=async: Celery 활용 비동기 처리 (기본)
- mode=sync: 즉시 RAG 처리 후 답변 반환
""",
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "question": {"type": "string"},
                "mode": {"type": "string", "enum": ["async", "sync"], "default": "async"}
            },
            "required": ["question"]
        }
    },
    responses={
        202: OpenApiResponse(
            description="비동기 모드 응답",
            examples=[
                OpenApiExample(
                    "Async Example",
                    value={"task_id": "123e4567-e89b-12d3-a456-426614174000", "status": "processing"}
                )
            ]
        ),
        200: OpenApiResponse(
            description="동기 모드 즉시 응답",
            examples=[
                OpenApiExample(
                    "Sync Example",
                    value={
                        "status": "done",
                        "result": {
                            "answer": "답변 내용...",
                            "contexts": [
                                {
                                    "chunk_id": "A01",
                                    "item_name": "Aspirin",
                                    "section": "효능",
                                    "chunk_index": 1
                                }
                            ]
                        }
                    }
                )
            ]
        ),
        400: OpenApiResponse(description="question 필드 누락"),
        500: OpenApiResponse(description="RAG 처리 중 서버 오류"),
    }
)
class DrugRAGView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        question = request.data.get("question")
        mode = request.data.get("mode", "async")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        if mode == "async":
            task = run_rag_task.delay(question)
            return Response({"task_id": task.id, "status": "processing"}, status=202)

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
            },
            status=200
        )


@extend_schema(
    tags=["RAG"],
    summary="RAG 비동기 task 결과 조회",
    description="비동기 모드(async) 요청의 RAG 작업 상태를 조회합니다.",
    parameters=[
        OpenApiParameter(
            name="task_id",
            type=str,
            location=OpenApiParameter.PATH,
            description="Celery Task ID"
        )
    ],
    responses={
        200: OpenApiResponse(
            examples=[
                OpenApiExample("Pending", value={"status": "pending"}),
                OpenApiExample("Processing", value={"status": "processing"}),
                OpenApiExample(
                    "Success",
                    value={
                        "status": "done",
                        "result": {"answer": "결과 텍스트...", "contexts": []}
                    }
                ),
                OpenApiExample(
                    "Failed",
                    value={"status": "failed", "error": "오류 내용"}
                ),
            ]
        )
    }
)
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

        return Response({"status": result.state}, status=200)
