# rag/docs.py
from drf_spectacular.utils import (
    extend_schema,
    OpenApiExample,
    OpenApiParameter,
    OpenApiResponse
)

# ============================
# RAG Query 문서
# ============================

rag_query_docs = extend_schema(
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
                "mode": {"type": "string", "enum": ["async", "sync"], "default": "async"},
            },
            "required": ["question"],
        }
    },
    responses={
        202: OpenApiResponse(
            description="비동기 모드 응답",
            examples=[
                OpenApiExample(
                    "Async Example",
                    value={
                        "task_id": "123e4567-e89b-12d3-a456-426614174000",
                        "status": "processing"
                    },
                )
            ],
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
                                    "chunk_index": 1,
                                }
                            ],
                        },
                    },
                )
            ],
        ),
    },
)

# ============================
# RAG Task Result 문서
# ============================

rag_result_docs = extend_schema(
    tags=["RAG"],
    summary="RAG 비동기 task 결과 조회",
    description="비동기 모드(async) 요청의 RAG 작업 상태를 조회합니다.",
    parameters=[
        OpenApiParameter(
            name="task_id",
            type=str,
            location=OpenApiParameter.PATH,
            description="Celery Task ID",
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
                        "result": {
                            "answer": "결과 텍스트...",
                            "contexts": []
                        }
                    }
                ),
                OpenApiExample(
                    "Failed",
                    value={"status": "failed", "error": "오류 내용"},
                ),
            ]
        )
    },
)
