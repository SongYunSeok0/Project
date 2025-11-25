from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
import datetime
from .serializers import PlanSerializer

from .models import Regihistory, Plan
from .serializers import (
    RegiHistorySerializer,
    RegiHistoryCreateSerializer,
    PlanCreateIn,
)

def to_ms(dt):
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(
            dt,
            datetime.time.min,
            tzinfo=timezone.get_current_timezone()
        )
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


# ============================================================
#  RegiHistory GET + POST
# ============================================================
class RegiHistoryListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    # GET → 내 RegiHistory 목록
    def get(self, request):
        rows = RegiHistory.objects.filter(user=request.user).order_by("-id")
        data = RegiHistorySerializer(rows, many=True).data
        return Response(data, status=status.HTTP_200_OK)

    # POST → 새 RegiHistory 생성
    def post(self, request):
        ser = RegiHistoryCreateSerializer(data=request.data, context={"request": request})
        ser.is_valid(raise_exception=True)
        regi = ser.save()  # user 자동 주입
        return Response(RegiHistorySerializer(regi).data, status=status.HTTP_201_CREATED)


# ============================================================
#  Plan GET + POST
# ============================================================
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    # ==========================
    #        GET (목록)
    # ==========================
    def get(self, request):
        # ✅ Plan.user 없음 → RegiHistory.user 기준으로 필터
        plans = Plan.objects.filter(
            regihistory__user=request.user
        )

        data = []
        for p in plans:
            data.append(
                {
                    "id": p.id,
                    "regihistoryId": p.regihistory.id if p.regihistory else None,
                    "medName": p.med_name,
                    "takenAt": to_ms(p.taken_at),
                    "mealTime": p.meal_time,
                    "note": p.note,
                    "taken": to_ms(p.taken),
                }
            )

        return Response(data, status=status.HTTP_200_OK)

    # ==========================
    #        POST (등록)
    # ==========================
    def post(self, request):
        ser = PlanCreateIn(data=request.data)
        ser.is_valid(raise_exception=True)
        v = ser.validated_data

        def to_dt(ms):
            if not ms:
                return None
            return datetime.datetime.fromtimestamp(
                ms / 1000,
                tz=timezone.get_current_timezone(),
            )

        # 🔁 이제는 regihistoryId 로 받는다고 가정
        regi_history_id = v.get("regihistoryId")
        regi_history = None
        if regi_history_id is not None:
            # 자신의 regihistory 것만 허용 (보안)
            regi_history = regihistory.objects.filter(
                id=regi_history_id,
                user=request.user
            ).first()

            if regi_history is None:
                return Response(
                    {"error": "등록 이력이 존재하지 않거나 권한이 없습니다."},
                    status=status.HTTP_400_BAD_REQUEST,
                )

        # Plan 생성
        plan = Plan.objects.create(
            regihistory=regi_history,
            med_name=v.get("medName"),
            taken_at=to_dt(v.get("takenAt")),
            meal_time=v.get("mealTime") or "before",
            note=v.get("note"),
            taken=to_dt(v.get("taken")),
        )

        return Response(PlanSerializer(plan).data, status=status.HTTP_201_CREATED)
