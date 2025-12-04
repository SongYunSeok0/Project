from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from django.utils.dateparse import parse_datetime
import datetime

from .models import RegiHistory, Plan
from .serializers import (
    RegiHistorySerializer,
    RegiHistoryCreateSerializer,
    PlanSerializer,
    PlanCreateIn,
)
from drf_spectacular.utils import (
    extend_schema,
    OpenApiExample,
    OpenApiParameter,
    OpenApiResponse
)

def to_ms(dt):
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(dt, datetime.time.min)
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, datetime.timezone.utc)
    return int(dt.timestamp() * 1000)

def to_dt(ms):
    if not ms:
        return None
    return datetime.datetime.fromtimestamp(ms / 1000, tz=datetime.timezone.utc)


@extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 목록 조회",
    description="현재 로그인된 사용자의 모든 RegiHistory(등록 이력)를 최신순으로 반환합니다.",
    responses={200: RegiHistorySerializer(many=True)},
)
class RegiHistoryListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    @extend_schema(
        summary="등록 이력 생성",
        description="RegiHistoryCreateSerializer 기반으로 새로운 등록 이력을 생성합니다.",
        request=RegiHistoryCreateSerializer,
        responses={
            201: RegiHistorySerializer,
            400: OpenApiResponse(description="유효성 검사 실패"),
        },
        examples=[
            OpenApiExample(
                "예시 요청",
                value={
                    "regi_type": "hospital",
                    "label": "고혈압",
                    "issued_date": "2025-12-03"
                }
            )
        ]
    )
    def post(self, request):
        ser = RegiHistoryCreateSerializer(data=request.data, context={"request": request})
        ser.is_valid(raise_exception=True)
        regi = ser.save()
        return Response(RegiHistorySerializer(regi).data, status=status.HTTP_201_CREATED)

    def get(self, request):
        rows = RegiHistory.objects.filter(user=request.user).order_by("-id")
        return Response(RegiHistorySerializer(rows, many=True).data, status=status.HTTP_200_OK)



@extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 수정",
    description="특정 등록 이력(RegiHistory)을 부분 업데이트(PATCH)합니다.",
    request=RegiHistoryCreateSerializer,
    parameters=[
        OpenApiParameter("pk", int, OpenApiParameter.PATH, description="RegiHistory ID")
    ],
    responses={
        200: RegiHistorySerializer,
        404: OpenApiResponse(description="not found"),
    }
)
class RegiHistoryUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        regi = RegiHistory.objects.filter(id=pk, user=request.user).first()
        if regi is None:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)

        ser = RegiHistoryCreateSerializer(regi, data=request.data, partial=True, context={"request": request})
        ser.is_valid(raise_exception=True)
        ser.save()
        return Response(RegiHistorySerializer(regi).data, status=status.HTTP_200_OK)



@extend_schema(
    tags=["RegiHistory"],
    summary="등록 이력 삭제",
    description="특정 RegiHistory를 삭제합니다.",
    parameters=[
        OpenApiParameter("pk", int, OpenApiParameter.PATH, description="RegiHistory ID")
    ],
    responses={
        204: OpenApiResponse(description="삭제 성공"),
        404: OpenApiResponse(description="not found"),
    }
)
class RegiHistoryDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        row = RegiHistory.objects.filter(id=pk, user=request.user).first()
        if row is None:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)
        row.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)



@extend_schema(
    tags=["Plan"],
    summary="플랜(복약 일정) 목록 조회",
    description="현재 사용자와 연결된 전체 복약 일정을 조회합니다.",
    responses={200: PlanSerializer(many=True)},
)
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = Plan.objects.filter(regihistory__user=request.user)
        return Response(PlanSerializer(plans, many=True).data, status=status.HTTP_200_OK)

    @extend_schema(
        tags=["Plan"],
        summary="플랜 생성 (단건 + 스마트 일괄 등록)",
        description="""
### 케이스 1) 스마트 일괄 생성(times[] 존재)
- regihistoryId  
- startDate (YYYY-MM-DD)  
- duration: 며칠 반복  
- times: 하루 내 시간 배열  
- medName

### 케이스 2) 단건 생성
- regihistoryId  
- medName  
- takenAt (timestamp ms)  
- mealTime  
- useAlarm  
        """,
        request={
            "application/json": {
                "oneOf": [
                    {   # 스마트 일괄 등록
                        "type": "object",
                        "properties": {
                            "regihistoryId": {"type": "integer"},
                            "startDate": {"type": "string"},
                            "duration": {"type": "integer"},
                            "times": {"type": "array", "items": {"type": "string"}},
                            "medName": {"type": "string"},
                        }
                    },
                    {   # 단건 등록
                        "type": "object",
                        "properties": {
                            "regihistoryId": {"type": "integer"},
                            "medName": {"type": "string"},
                            "takenAt": {"type": "integer", "description": "timestamp(ms)"},
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
            201: OpenApiResponse(description="생성 성공", response=PlanSerializer(many=True)),
            400: OpenApiResponse(description="유효성 실패 또는 권한 없음"),
        },
        examples=[
            OpenApiExample(
                "스마트 일괄 등록",
                value={
                    "regihistoryId": 3,
                    "startDate": "2025-12-05",
                    "duration": 5,
                    "times": ["09:00", "21:00"],
                    "medName": "혈압약"
                }
            ),
            OpenApiExample(
                "단건 등록",
                value={
                    "regihistoryId": 3,
                    "medName": "혈압약",
                    "takenAt": 1733204853000,
                    "mealTime": "before",
                    "useAlarm": True
                }
            )
        ]
    )
    def post(self, request):
        # (기존 로직 그대로)
        return super().post(request)


@extend_schema(
    tags=["Plan"],
    summary="플랜 삭제",
    description="특정 복약 일정을 삭제합니다.",
    parameters=[OpenApiParameter("pk", int, OpenApiParameter.PATH)],
    responses={
        204: OpenApiResponse(description="삭제 성공"),
        404: OpenApiResponse(description="not found")
    }
)
class PlanDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if plan is None:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)
        plan.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


@extend_schema(
    tags=["Plan"],
    summary="오늘의 복약 일정 조회",
    description="오늘 날짜 기준으로 복약 일정 조회, 완료 여부(taken/missed/pending)와 함께 반환합니다.",
    responses={200: PlanSerializer(many=True)},
)
class TodayPlansView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        now = timezone.now()
        start = now.replace(hour=0, minute=0, second=0, microsecond=0)
        end = start + datetime.timedelta(days=1)

        plans = Plan.objects.filter(
            regihistory__user=request.user,
            taken_at__gte=start,
            taken_at__lt=end
        ).order_by("taken_at")

        result = []
        for p in plans:
            if p.taken is not None:
                status_str = "taken"
            elif now > p.taken_at + datetime.timedelta(hours=1):
                status_str = "missed"
            else:
                status_str = "pending"

            item = PlanSerializer(p).data
            item["status"] = status_str
            result.append(item)

        return Response(result, status=status.HTTP_200_OK)


@extend_schema(
    tags=["Plan"],
    summary="플랜 수정",
    description="특정 복약 일정을 부분 수정합니다. takenAt 변경 시 그룹 일정도 자동 이동됩니다.",
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
    },
)
class PlanUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        target_plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if not target_plan:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)

        data = request.data

        if "takenAt" in data:
            raw_taken_at = data["takenAt"]
            if isinstance(raw_taken_at, (int, float)):
                # 타임스탬프 처리 (tz=datetime.timezone.utc 사용)
                new_taken_at = datetime.datetime.fromtimestamp(raw_taken_at / 1000.0, tz=datetime.timezone.utc)
            else:
                new_taken_at = parse_datetime(raw_taken_at)

            old_taken_at = target_plan.taken_at

            # ✅ 그룹 식별용 '기존 수정 시간' 저장
            old_updated_at = target_plan.updated_at

            # --- 타겟 먼저 업데이트 (updated_at 갱신됨) ---
            target_plan.taken_at = new_taken_at
            if "medName" in data: target_plan.med_name = data["medName"]
            if "useAlarm" in data: target_plan.use_alarm = data["useAlarm"]
            target_plan.save()

            # --- 같은 그룹(형제들) 찾아서 동기화 ---
            if old_taken_at and target_plan.regihistory:
                siblings = Plan.objects.filter(
                    regihistory=target_plan.regihistory,
                    taken_at=old_taken_at,
                    updated_at=old_updated_at  # ✅ 같은 배치(Batch)로 수정된 애들만 찾음
                ).exclude(id=target_plan.id)

                count = siblings.update(
                    taken_at=new_taken_at,
                    # ✅ 형제들도 타겟과 똑같은 updated_at을 갖도록 강제 동기화
                    updated_at=target_plan.updated_at
                )
                print(f"[Plan Update] updated_at={old_updated_at} 그룹에서 {count}개 이동됨.")

        else:
            # 시간 변경 없는 경우
            if "medName" in data: target_plan.med_name = data["medName"]
            if "useAlarm" in data: target_plan.use_alarm = data["useAlarm"]
            target_plan.save()

        return Response(PlanSerializer(target_plan).data, status=status.HTTP_200_OK)
