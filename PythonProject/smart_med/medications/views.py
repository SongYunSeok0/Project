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


# RegiHistory GET + POST
class RegiHistoryListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        rows = RegiHistory.objects.filter(user=request.user).order_by("-id")
        return Response(RegiHistorySerializer(rows, many=True).data, status=status.HTTP_200_OK)

    def post(self, request):
        ser = RegiHistoryCreateSerializer(data=request.data, context={"request": request})
        ser.is_valid(raise_exception=True)
        regi = ser.save()
        return Response(RegiHistorySerializer(regi).data, status=status.HTTP_201_CREATED)


# RegiHistory PATCH
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


# RegiHistory DELETE
class RegiHistoryDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        row = RegiHistory.objects.filter(id=pk, user=request.user).first()
        if row is None:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)
        row.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


# Plan GET + POST
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = Plan.objects.filter(regihistory__user=request.user)
        return Response(PlanSerializer(plans, many=True).data, status=status.HTTP_200_OK)

    def post(self, request):
        ser = PlanCreateIn(data=request.data)
        ser.is_valid(raise_exception=True)
        v = ser.validated_data

        regi_history = None
        rid = v.get("regihistoryId")
        if rid is not None:
            regi_history = RegiHistory.objects.filter(id=rid, user=request.user).first()
            if regi_history is None:
                return Response({"error": "no permission"}, status=status.HTTP_400_BAD_REQUEST)

        plan = Plan.objects.create(
            regihistory=regi_history,
            med_name=v.get("medName"),
            taken_at=to_dt(v.get("takenAt")),
            meal_time=v.get("mealTime") or "before",
            note=v.get("note"),
            taken=to_dt(v.get("taken")),
            use_alarm=v.get("useAlarm", True),
        )

        return Response(PlanSerializer(plan).data, status=status.HTTP_201_CREATED)


# # Plan PATCH
# class PlanUpdateView(APIView):
#     permission_classes = [IsAuthenticated]
#
#     def patch(self, request, pk):
#         plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
#         if plan is None:
#             return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)
#
#         data = request.data
#
#         if "medName" in data:
#             plan.med_name = data["medName"]
#         if "takenAt" in data:
#             plan.taken_at = to_dt(data["takenAt"])
#         if "mealTime" in data:
#             plan.meal_time = data["mealTime"]
#         if "note" in data:
#             plan.note = data["note"]
#         if "taken" in data:
#             plan.taken = to_dt(data["taken"])
#         if "useAlarm" in data:
#             plan.use_alarm = data["useAlarm"]
#
#         plan.save()
#         return Response(PlanSerializer(plan).data, status=status.HTTP_200_OK)


# Plan DELETE
class PlanDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if plan is None:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)
        plan.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


# Today plans
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
                new_taken_at = datetime.datetime.fromtimestamp(raw_taken_at / 1000.0, tz=datetime.timezone.utc)
            else:
                new_taken_at = parse_datetime(raw_taken_at)

            old_taken_at = target_plan.taken_at
            old_created_at = target_plan.created_at   # ★ 그룹 기준 이것으로 변경

            # --- 타겟 시간 업데이트 ---
            target_plan.taken_at = new_taken_at
            if "medName" in data: target_plan.med_name = data["medName"]
            if "useAlarm" in data: target_plan.use_alarm = data["useAlarm"]
            target_plan.save()

            # --- 형제 일정 이동 ---
            siblings = Plan.objects.filter(
                regihistory=target_plan.regihistory,
                taken_at=old_taken_at,
                created_at=old_created_at
            ).exclude(id=target_plan.id)

            count = siblings.update(
                taken_at=new_taken_at
            )
            print(f"[Plan Update] created_at={old_created_at} 그룹에서 {count}개 이동됨.")

        else:
            # 시간 변경 없는 경우
            if "medName" in data: target_plan.med_name = data["medName"]
            if "useAlarm" in data: target_plan.use_alarm = data["useAlarm"]
            target_plan.save()

        return Response(PlanSerializer(target_plan).data, status=status.HTTP_200_OK)