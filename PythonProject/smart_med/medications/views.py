from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .models import Prescription, Plan
from .serializers import PlanCreateIn
import datetime

def to_ms(dt):
    """datetime → millisecond 변환"""
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(dt, datetime.time.min, tzinfo=timezone.get_current_timezone())
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = Plan.objects.filter(user=request.user).order_by("-created_at")

        data = []
        for p in plans:
            data.append({
                "id": p.id,
                "userId": p.user.id,
                "prescriptionId": p.prescription.id if p.prescription else None,
                "medName": p.med_name,
                "takenAt": to_ms(p.taken_at),
                "mealTime": p.meal_time,
                "note": p.note,
                "taken": to_ms(p.taken),       # time → ms (00:00 기반)
                "createdAt": to_ms(p.created_at),
                "updatedAt": to_ms(p.updated_at),
            })

        return Response(data, status=status.HTTP_200_OK)


    # ==========================
    #        POST (등록)
    # ==========================
    def post(self, request):
        ser = PlanCreateIn(data=request.data)
        ser.is_valid(raise_exception=True)
        v = ser.validated_data

        to_dt = lambda ms: datetime.datetime.fromtimestamp(ms / 1000,
                                                           tz=timezone.get_current_timezone()) if ms else None

        user_id = request.user.id  # 서버에서 결정
        prescription_id = v.get("prescriptionId", None)
        med_name = v.get("medName")
        taken_at = to_dt(v.get("takenAt"))
        meal_time = v.get("mealTime") or "none"
        note = v.get("note")
        taken = to_dt(v.get("taken"))

        # ❗ created_at / updated_at 넣지 않는다
        plan = Plan.objects.create(
            user_id=user_id,
            prescription_id=prescription_id,
            med_name=med_name,
            taken_at=taken_at,
            meal_time=meal_time,
            note=note,
            taken=taken,
        )

        return Response({"id": plan.id}, status=status.HTTP_201_CREATED)



