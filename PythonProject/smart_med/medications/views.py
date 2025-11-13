from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .models import Medication, MedicationSchedule
from .serializers import PlanCreateIn
import datetime

def to_ms(dt):
    if dt is None: return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(dt, datetime.time.min, tzinfo=timezone.get_current_timezone())
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)

class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        meds = (Medication.objects
                .filter(user=request.user)
                .prefetch_related("medicationschedule_set")
                .order_by("-created_at"))
        data = []
        for m in meds:
            scheds = list(m.medicationschedule_set.filter(is_active=True).order_by("time"))
            data.append({
                "id": m.id,
                "userId": str(m.user_id),
                "type": "SUPPLEMENT",
                "diseaseName": None,
                "supplementName": m.name,
                "dosePerDay": len(scheds),
                "mealRelation": "NONE",
                "memo": m.note or None,
                "startDay": to_ms(m.start_date),
                "endDay": to_ms(m.end_date),
                "createdAt": to_ms(m.created_at),
                "updatedAt": to_ms(m.updated_at),
                "times": [
                    {"time": (s.time.strftime("%H:%M:%S") if s.time else None),
                     "days_of_week": (s.days_of_week or [])}
                    for s in scheds
                ],
            })
        return Response(data, status=200)

    def post(self, request):
        ser = PlanCreateIn(data=request.data)
        ser.is_valid(raise_exception=True)
        v = ser.validated_data

        name = v["supplementName"]
        memo = v.get("memo") or ""
        start_date = ser.to_date(v.get("startDay")) or datetime.date.today()
        end_date = ser.to_date(v.get("endDay"))

        # Medication 생성
        m = Medication.objects.create(
            user=request.user,
            name=name,
            dose="",                    # 필요시 v에서 받도록 확장
            start_date=start_date,
            end_date=end_date,
            note=memo,
            source="manual",
        )

        # 스케줄 생성
        for t in v.get("times", []):
            time_obj = PlanCreateIn.fields["times"].child.to_python_time(
                PlanCreateIn.fields["times"].child.fields["time"].to_representation(t["time"])
                if isinstance(t["time"], datetime.time) else t["time"]
            )
            MedicationSchedule.objects.create(
                medication=m,
                user=request.user,
                time=time_obj,
                days_of_week=t.get("days_of_week") or [],
                is_active=True,
            )

        return Response({"id": m.id}, status=status.HTTP_201_CREATED)
