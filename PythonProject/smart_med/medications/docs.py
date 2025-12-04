from drf_spectacular.utils import (
    extend_schema,
    OpenApiExample,
    OpenApiParameter,
    OpenApiResponse,
)
from .serializers import (
    RegiHistorySerializer,
    RegiHistoryCreateSerializer,
    PlanSerializer,
)

# ============================
# RegiHistory 문서
# ============================
regi_list_docs = extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 목록 조회",
    description="사용자의 모든 등록 이력(RegiHistory)을 최신순으로 반환합니다.",
    responses={200: RegiHistorySerializer(many=True)},
)

regi_create_docs = extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 생성",
    description="RegiHistoryCreateSerializer 기준으로 새로운 이력을 생성합니다.",
    request=RegiHistoryCreateSerializer,
    responses={
        201: RegiHistorySerializer,
        400: OpenApiResponse(description="유효성 검사 실패"),
    },
    examples=[
        OpenApiExample(
            "예시 요청",
            value={"regi_type": "hospital", "label": "고혈압", "issued_date": "2025-12-03"}
        )
    ]
)

regi_update_docs = extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 수정",
    description="특정 RegiHistory를 부분 수정합니다.",
    request=RegiHistoryCreateSerializer,
    parameters=[OpenApiParameter("pk", int, OpenApiParameter.PATH)],
    responses={
        200: RegiHistorySerializer,
        404: OpenApiResponse(description="not found"),
    }
)

regi_delete_docs = extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 삭제",
    parameters=[OpenApiParameter("pk", int, OpenApiParameter.PATH)],
    responses={
        204: OpenApiResponse(description="삭제 성공"),
        404: OpenApiResponse(description="not found"),
    },
)

# ============================
# Plan 문서
# ============================
plan_list_docs = extend_schema(
    tags=["Plan"],
    summary="플랜 목록 조회",
    description="사용자의 전체 복약 일정을 반환합니다.",
    responses={200: PlanSerializer(many=True)},
)

plan_create_docs = extend_schema(
    tags=["Plan"],
    summary="플랜 생성 (단건 or 스마트 일괄)",
    description="""
### 📌 스마트 일괄(times[] 존재)
- regihistoryId  
- startDate  
- duration  
- times[]  
- medName  

### 📌 단건
- regihistoryId  
- medName  
- takenAt(ms)  
- mealTime  
- useAlarm  
    """,
    request={
        "application/json": {
            "oneOf": [
                {
                    "type": "object",
                    "properties": {
                        "regihistoryId": {"type": "integer"},
                        "startDate": {"type": "string"},
                        "duration": {"type": "integer"},
                        "times": {"type": "array", "items": {"type": "string"}},
                        "medName": {"type": "string"},
                    }
                },
                {
                    "type": "object",
                    "properties": {
                        "regihistoryId": {"type": "integer"},
                        "medName": {"type": "string"},
                        "takenAt": {"type": "integer"},
                        "mealTime": {"type": "string"},
                        "note": {"type": "string"},
                        "taken": {"type": "integer"},
                        "useAlarm": {"type": "boolean"},
                    }
                }
            ]
        }
    },
    responses={
        201: PlanSerializer(many=True),
        400: OpenApiResponse(description="유효성 실패"),
    }
)

plan_delete_docs = extend_schema(
    tags=["Plan"],
    summary="플랜 삭제",
    parameters=[OpenApiParameter("pk", int, OpenApiParameter.PATH)],
    responses={204: None, 404: OpenApiResponse(description="not found")},
)

plan_today_docs = extend_schema(
    tags=["Plan"],
    summary="오늘의 복약 일정 조회",
    description="오늘 날짜 기준으로 pending / taken / missed 포함한 플랜 리스트 반환.",
    responses={200: PlanSerializer(many=True)},
)

plan_update_docs = extend_schema(
    tags=["Plan"],
    summary="플랜 수정",
    description="특정 일정의 takenAt 변경 시 같은 그룹도 자동 이동됩니다.",
    parameters=[OpenApiParameter("pk", int, OpenApiParameter.PATH)],
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "takenAt": {"type": "integer"},
                "medName": {"type": "string"},
                "useAlarm": {"type": "boolean"},
            }
        }
    },
    responses={
        200: PlanSerializer,
        404: OpenApiResponse(description="not found")
    }
)
