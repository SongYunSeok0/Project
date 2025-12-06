# med/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status

from django.utils import timezone
from django.utils.dateparse import parse_datetime
from django.shortcuts import get_object_or_404
import datetime

from smart_med.utils.time_utils import to_ms, from_ms
from .models import RegiHistory, Plan
from .serializers import (
    RegiHistorySerializer,
    RegiHistoryCreateSerializer,
    PlanSerializer,
    PlanCreateIn,
)

from .docs import (
    regi_list_docs, regi_create_docs, regi_update_docs, regi_delete_docs,
    plan_list_docs, plan_create_docs, plan_delete_docs,
    plan_today_docs, plan_update_docs,
    mark_as_taken_docs, snooze_docs
)

# ============================================================
# ✔ RegiHistory (CRUD)
# ============================================================

@regi_list_docs
class RegiHistoryListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    @regi_create_docs
    def post(self, request):
        ser = RegiHistoryCreateSerializer(
            data=request.data,
            context={"request": request}
        )
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


# ============================================================
# ✔ Plan (GET / POST 단건 + 스마트 일정 생성)
# ============================================================

@plan_list_docs
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = Plan.objects.filter(regihistory__user=request.user)
        return Response(PlanSerializer(plans, many=True).data)

    @plan_create_docs
    def post(self, request):
        data = request.data

        # ============================================================
        # Case 1: 스마트 일정 일괄 등록
        # ============================================================
        if "times" in data and isinstance(data["times"], list):
            rid = data.get("regihistoryId")
            start_date_str = data.get("startDate")
            duration = int(data.get("duration", 1))
            times = data.get("times", [])
            med_name = data.get("medName", "")

            regi = RegiHistory.objects.filter(id=rid, user=request.user).first()
            if not regi:
                return Response({"error": "RegiHistory not found"}, status=404)

            try:
                current_date = datetime.datetime.strptime(start_date_str, "%Y-%m-%d").date()
            except:
                current_date = timezone.localdate()

            now = timezone.now()
            created_plans = []
            total_target = duration * len(times)
            count = 0

            while count < total_target:
                for t in sorted(times):
                    if count >= total_target:
                        break

                    hour, minute = map(int, t.split(":"))
                    dt = datetime.datetime.combine(current_date, datetime.time(hour, minute))

                    if timezone.is_naive(dt):
                        dt = timezone.make_aware(dt, timezone.get_current_timezone())

                    if dt <= now:
                        continue

                    p = Plan.objects.create(
                        regihistory=regi,
                        med_name=med_name,
                        taken_at=dt,
                        ex_taken_at=dt,
                        meal_time="after",
                        use_alarm=True,
                    )
                    created_plans.append(p)
                    count += 1

                current_date += datetime.timedelta(days=1)

            if created_plans:
                sync = timezone.now()
                Plan.objects.filter(id__in=[p.id for p in created_plans]).update(updated_at=sync)
                for p in created_plans:
                    p.updated_at = sync

            return Response({
                "message": f"{len(created_plans)}개의 스마트 일정이 생성되었습니다.",
                "plans": PlanSerializer(created_plans, many=True).data
            }, status=201)

        # ============================================================
        # Case 2: 단건 등록
        # ============================================================
        ser = PlanCreateIn(data=data)
        ser.is_valid(raise_exception=True)
        v = ser.validated_data

        regi = RegiHistory.objects.filter(id=v["regihistoryId"], user=request.user).first()
        if not regi:
            return Response({"error": "no permission"}, status=400)

        dt = from_ms(v.get("takenAt"))

        plan = Plan.objects.create(
            regihistory=regi,
            med_name=v.get("medName"),
            taken_at=dt,
            ex_taken_at=dt,
            meal_time=v.get("mealTime") or "before",
            note=v.get("note"),
            taken=from_ms(v.get("taken")),
            use_alarm=v.get("useAlarm", True),
        )

        return Response(PlanSerializer(plan).data, status=201)


# ============================================================
# ✔ Plan DELETE
# ============================================================

@plan_delete_docs
class PlanDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if not plan:
            return Response({"error": "not found"}, status=404)

        plan.delete()
        return Response(status=204)


# ============================================================
# ✔ 오늘 복약 일정 조회
# ============================================================

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
            taken_at__lt=end
        ).order_by("taken_at")

        result = []
        for p in plans:
            if p.taken:
                status_str = "taken"
            elif now > p.taken_at + datetime.timedelta(hours=1):
                status_str = "missed"
            else:
                status_str = "pending"

            item = PlanSerializer(p).data
            item["status"] = status_str
            result.append(item)

        return Response(result)


# ============================================================
# ✔ Plan Update
# ============================================================

@plan_update_docs
class PlanUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if not plan:
            return Response({"error": "not found"}, status=404)

        data = request.data

        if "takenAt" in data:
            raw = data["takenAt"]

            if isinstance(raw, (int, float)):
                new_dt = datetime.datetime.fromtimestamp(raw / 1000, tz=datetime.timezone.utc)
            else:
                new_dt = parse_datetime(raw)

            old_dt = plan.taken_at
            old_updated = plan.updated_at

            plan.taken_at = new_dt
            if "medName" in data:
                plan.med_name = data["medName"]
            if "useAlarm" in data:
                plan.use_alarm = data["useAlarm"]
            plan.save()

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
            if "medName" in data:
                plan.med_name = data["medName"]
            if "useAlarm" in data:
                plan.use_alarm = data["useAlarm"]
            plan.save()

        return Response(PlanSerializer(plan).data, status=200)


# ============================================================
# ✔ 복약 완료
# ============================================================

@mark_as_taken_docs
class MarkAsTakenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, plan_id):
        plan = get_object_or_404(Plan, id=plan_id, regihistory__user=request.user)

        if plan.taken:
            return Response({"message": "이미 복약 완료됨"}, status=200)

        plan.taken = timezone.now()
        plan.save()

        return Response({
            "message": "복약 완료 처리됨",
            "taken_time": plan.taken
        }, status=200)


# ============================================================
# ✔ 미루기 (30분 뒤로)
# ============================================================

@snooze_docs
class SnoozeMedicationView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, plan_id):
        plan = get_object_or_404(Plan, id=plan_id, regihistory__user=request.user)

        if plan.taken:
            return Response({"error": "이미 복약됨"}, status=400)

        plan.taken_at = plan.taken_at + datetime.timedelta(minutes=30)
        plan.save()

        return Response({
            "message": "복약 알림이 30분 뒤로 미뤄졌습니다.",
            "new_taken_at": plan.taken_at
        }, status=200)
