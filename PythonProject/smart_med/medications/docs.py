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
    request=RegiHistoryCreateSerializer,
    parameters=[OpenApiParameter("pk", int, location=OpenApiParameter.PATH)],
    responses={
        200: RegiHistorySerializer,
        404: OpenApiResponse(description="not found"),
    }
)

regi_delete_docs = extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 삭제",
    parameters=[OpenApiParameter("pk", int, location=OpenApiParameter.PATH)],
    responses={
        204: None,
        404: OpenApiResponse(description="not found"),
    },
)

# ============================
# Plan 문서
# ============================
plan_list_docs = extend_schema(
    tags=["Plan"],
    summary="플랜 목록 조회",
    description="사용자의 전체 복약 플랜을 반환합니다.",
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
    parameters=[OpenApiParameter("pk", int, location=OpenApiParameter.PATH)],
    responses={
        204: None,
        404: OpenApiResponse(description="not found"),
    },
)

plan_today_docs = extend_schema(
    tags=["Plan"],
    summary="오늘의 복약 일정 조회",
    description="오늘 날짜의 pending/taken/missed 포함한 플랜 리스트를 반환합니다.",
    responses={200: PlanSerializer(many=True)},
)

plan_update_docs = extend_schema(
    tags=["Plan"],
    summary="플랜 수정",
    description="takenAt 변경 시 동일 그룹 일정도 자동 업데이트됩니다.",
    parameters=[OpenApiParameter("pk", int, location=OpenApiParameter.PATH)],
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "takenAt": {"type": "integer"},
                "medName": {"type": "string"},
                "useAlarm": {"type": "boolean"},
            },
        }
    },
    responses={
        200: PlanSerializer,
        404: OpenApiResponse(description="not found")
    }
)

# ============================
# Mark as Taken
# ============================
mark_as_taken_docs = extend_schema(
    tags=["Plan"],
    summary="복약 완료 처리",
    parameters=[OpenApiParameter("plan_id", int, location=OpenApiParameter.PATH)],
    responses={
        200: OpenApiResponse(
            response={
                "type": "object",
                "properties": {
                    "message": {"type": "string"},
                    "taken_time": {"type": "string", "format": "date-time"},
                },
            }
        ),
        404: OpenApiResponse(description="not found")
    }
)

# ============================
# Snooze
# ============================
snooze_docs = extend_schema(
    tags=["Plan"],
    summary="복약 알림을 30분 뒤로 미루기",
    parameters=[OpenApiParameter("plan_id", int, location=OpenApiParameter.PATH)],
    responses={
        200: OpenApiResponse(
            response={
                "type": "object",
                "properties": {
                    "message": {"type": "string"},
                    "new_taken_at": {"type": "string", "format": "date-time"},
                },
            }
        ),
        400: OpenApiResponse(description="이미 복약됨"),
        404: OpenApiResponse(description="not found"),
    }
)
