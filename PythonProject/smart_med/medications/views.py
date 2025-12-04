# med/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
import datetime
from django.utils import timezone
from django.utils.dateparse import parse_datetime

from smart_med.utils.time_utils import from_ms
from .models import RegiHistory, Plan
from .serializers import (
    RegiHistorySerializer,
    RegiHistoryCreateSerializer,
    PlanSerializer
)
from .docs import (
    regi_list_docs, regi_create_docs, regi_update_docs, regi_delete_docs,
    plan_list_docs, plan_create_docs, plan_delete_docs,
    plan_today_docs, plan_update_docs
)

# ========================
# RegiHistory
# ========================

@regi_list_docs
class RegiHistoryListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    @regi_create_docs
    def post(self, request):
        ser = RegiHistoryCreateSerializer(data=request.data, context={"request": request})
        ser.is_valid(raise_exception=True)
        obj = ser.save()
        return Response(RegiHistorySerializer(obj).data, status=201)

    def get(self, request):
        rows = RegiHistory.objects.filter(user=request.user).order_by("-id")
        return Response(RegiHistorySerializer(rows, many=True).data)


@regi_update_docs
class RegiHistoryUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        obj = RegiHistory.objects.filter(id=pk, user=request.user).first()
        if not obj:
            return Response({"error": "not found"}, status=404)

        ser = RegiHistoryCreateSerializer(obj, data=request.data, partial=True)
        ser.is_valid(raise_exception=True)
        ser.save()
        return Response(RegiHistorySerializer(obj).data)


@regi_delete_docs
class RegiHistoryDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        row = RegiHistory.objects.filter(id=pk, user=request.user).first()
        if not row:
            return Response({"error": "not found"}, status=404)
        row.delete()
        return Response(status=204)

# ========================
# Plan
# ========================

@plan_list_docs
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    @plan_create_docs
    def post(self, request):
        regihistory_id = request.data.get("regihistoryId")
        med_name = request.data.get("medName")
        taken_at = request.data.get("takenAt")
        meal_time = request.data.get("mealTime")
        use_alarm = request.data.get("useAlarm", True)

        # RegiHistory 검증
        try:
            regihistory = RegiHistory.objects.get(
                id=regihistory_id,
                user=request.user
            )
        except RegiHistory.DoesNotExist:
            return Response({"error": "Invalid regihistoryId"}, status=404)

        # timestamp(ms) → datetime 변환
        taken_at_dt = from_ms(taken_at)

        # Plan 생성
        plan = Plan.objects.create(
            regihistory=regihistory,
            med_name=med_name,
            taken_at=taken_at_dt,
            meal_time=meal_time,
            use_alarm=use_alarm,
        )

        return Response(
            PlanSerializer(plan).data,
            status=201
        )

    def get(self, request):
        plans = Plan.objects.filter(regihistory__user=request.user)
        return Response(PlanSerializer(plans, many=True).data)


@plan_delete_docs
class PlanDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if not plan:
            return Response({"error": "not found"}, status=404)
        plan.delete()
        return Response(status=204)


@plan_today_docs
class TodayPlansView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        now = timezone.now()
        start = now.replace(hour=0, minute=0, second=0, microsecond=0)
        end = start + datetime.timedelta(days=1)

        plans = Plan.objects.filter(
            regihistory__user=request.user,
            taken_at__gte=start,
            taken_at__lt=end,
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

        return Response(result)


@plan_update_docs
class PlanUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if not plan:
            return Response({"error": "not found"}, status=404)

        data = request.data

        # takenAt 변경 처리
        if "takenAt" in data:
            raw = data["takenAt"]
            if isinstance(raw, (int, float)):
                new_dt = datetime.datetime.fromtimestamp(raw / 1000, tz=datetime.timezone.utc)
            else:
                new_dt = parse_datetime(raw)

            old_dt = plan.taken_at
            old_updated = plan.updated_at

            plan.taken_at = new_dt
            if "medName" in data: plan.med_name = data["medName"]
            if "useAlarm" in data: plan.use_alarm = data["useAlarm"]
            plan.save()

            # 그룹 이동
            siblings = Plan.objects.filter(
                regihistory=plan.regihistory,
                taken_at=old_dt,
                updated_at=old_updated
            ).exclude(id=plan.id)

            siblings.update(
                taken_at=new_dt,
                updated_at=plan.updated_at
            )

        else:
            if "medName" in data: plan.med_name = data["medName"]
            if "useAlarm" in data: plan.use_alarm = data["useAlarm"]
            plan.save()

        return Response(PlanSerializer(plan).data)
