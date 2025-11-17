from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .models import Prescription, Plan
from .serializers import PlanCreateIn
import datetime


def to_ms(dt):
    """datetime → millisecond 변환 (프론트 전송용)"""
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(dt, datetime.time.min, tzinfo=timezone.get_current_timezone())
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    # ✅ 복용 스케줄 목록 조회
    def get(self, request):
        plans = (
            Plan.objects
            .filter(user_id=request.user.id)
            .select_related("prescription")
            .order_by("-taken_at", "-created_at")
        )

        data = []
        for p in plans:
            data.append({
                "id": p.user,
                "prescriptionId": p.prescription,
                "medicineName": p.med_name,
                "alarmTime": p.taken_at,
                "mealRelation": p.meal_time or "NONE",
                "note": p.note or None,
                "taken": p.taken or None,
            })

        return Response(data, status=status.HTTP_200_OK)

    # ✅ 복용 스케줄 등록
    def post(self, request):
        ser = PlanCreateIn(data=request.data)
        ser.is_valid(raise_exception=True)
        v = ser.validated_data

        prescription_id = v.get("prescriptionId")
        medicine_name = v.get("medicineName")
        memo = v.get("memo", "")
        meal_relation = v.get("mealRelation", "NONE")
        taken = v.get("taken", None)
        alarm_time = v.get("alarmTime")


        # ✅ MedicationPlan 생성
        plan = Plan.objects.create(
            user_id=request.user.id,
            prescription_id=prescription_id,
            medicine_name=medicine_name,
            alarm_time=alarm_time,
            meal_relation=meal_relation,
            memo=memo,
            taken=taken,
        )

        return Response({"id": plan.prescription}, status=status.HTTP_201_CREATED)
