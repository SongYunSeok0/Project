# plans/views.py (예시)
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .models import regihistory, Plan
from .serializers import PlanCreateIn
import datetime


def to_ms(dt):
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(
            dt,
            datetime.time.min,
            tzinfo=timezone.get_current_timezone(),
        )
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    # ==========================
    #        GET (목록)
    # ==========================
    def get(self, request):
        # ✅ Plan.user 없음 → regihistory.user 기준으로 필터
        plans = Plan.objects.filter(
            regihistory__user=request.user.id
        ).order_by("-created_at")

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
                    "createdAt": to_ms(p.created_at),
                    "updatedAt": to_ms(p.updated_at),
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
                ms / 1000, tz=timezone.get_current_timezone()
            )

        # 🔁 이제는 regihistoryId 로 받는다고 가정
        regi_history_id = v.get("regihistoryId")
        regi_history = None
        if regi_history_id is not None:
            # 자신의 regihistory 것만 허용 (보안)
            regi_history = regihistory.objects.filter(
                id=regi_history_id,
                user=request.user.id,
            ).first()

        # 만약 안드로이드에서 regihistoryId를 안 보내면 (또는 그런 기능 아직 없음)
        # 여기서 자동 생성해 줄 수 있음
        if regi_history is None:
            regi_history = regihistory.objects.create(
                user=request.user,
                regi_type="직접등록",  # 네가 쓸 타입 문자열
                label=v.get("medName") or "직접등록",  # 예: 약 이름
                issued_date=timezone.now().date().isoformat(),
            )

        med_name = v.get("medName")
        taken_at = to_dt(v.get("takenAt"))
        meal_time = v.get("mealTime") or "before"  # 기본값 하나 정해두기
        note = v.get("note")
        taken = to_dt(v.get("taken"))

        plan = Plan.objects.create(
            regihistory=regi_history,
            med_name=med_name,
            taken_at=taken_at,
            meal_time=meal_time,
            note=note,
            taken=taken,
        )

        return Response({"id": plan.id}, status=status.HTTP_201_CREATED)
