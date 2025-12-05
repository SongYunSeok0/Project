from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.shortcuts import get_object_or_404
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
        data = request.data

        # [Case 1] 스마트 일괄 등록
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
                print(f"[Plan Create] 날짜 파싱 실패: {start_date_str}")
                current_date = timezone.now().date()

            now = timezone.now()
            total_count = duration * len(times)
            created_count = 0
            created_plans = []

            max_loop_days = duration * 3
            days_looped = 0

            while created_count < total_count and days_looped < max_loop_days:
                for t_str in sorted(times):
                    if created_count >= total_count:
                        break

                    try:
                        hour, minute = map(int, t_str.split(":"))
                        plan_dt = datetime.datetime.combine(current_date, datetime.time(hour, minute))

                        if timezone.is_naive(plan_dt):
                            plan_dt = timezone.make_aware(plan_dt, timezone.get_current_timezone())

                        if plan_dt > now:
                            p = Plan.objects.create(
                                regihistory=regi,
                                med_name=med_name,
                                taken_at=plan_dt,
                                ex_taken_at=plan_dt,
                                use_alarm=True,
                                meal_time="after"
                            )
                            created_plans.append(p)
                            created_count += 1
                    except Exception as e:
                        print(f"⚠️ [ERROR] 시간 처리 중 오류: {e}")
                        continue

                current_date += datetime.timedelta(days=1)
                days_looped += 1

            if created_plans:
                sync_time = timezone.now()
                Plan.objects.filter(id__in=[p.id for p in created_plans]).update(updated_at=sync_time)
                for p in created_plans:
                    p.updated_at = sync_time

            return Response({
                "message": f"총 {created_count}개의 스마트 일정이 생성되었습니다.",
                "plans": PlanSerializer(created_plans, many=True).data
            }, status=status.HTTP_201_CREATED)

        # [Case 2] 단건 등록
        else:
            ser = PlanCreateIn(data=data)
            ser.is_valid(raise_exception=True)
            v = ser.validated_data

            regi_history = None
            rid = v.get("regihistoryId")
            if rid is not None:
                regi_history = RegiHistory.objects.filter(id=rid, user=request.user).first()
                if regi_history is None:
                    return Response({"error": "no permission"}, status=status.HTTP_400_BAD_REQUEST)

            taken_at_value = to_dt(v.get("takenAt"))

            plan = Plan.objects.create(
                regihistory=regi_history,
                med_name=v.get("medName"),
                taken_at=taken_at_value,
                ex_taken_at=taken_at_value,
                meal_time=v.get("mealTime") or "before",
                note=v.get("note"),
                taken=to_dt(v.get("taken")),
                use_alarm=v.get("useAlarm", True),
            )

            return Response(PlanSerializer(plan).data, status=status.HTTP_201_CREATED)


# Plan DELETE
class PlanDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        plan = Plan.objects.filter(id=pk, regihistory__user=request.user).first()
        if plan is None:
            return Response({"error": "not found"}, status=status.HTTP_404_NOT_FOUND)
        plan.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


# Plan UPDATE
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
            old_updated_at = target_plan.updated_at

            target_plan.taken_at = new_taken_at
            if "medName" in data: target_plan.med_name = data["medName"]
            if "useAlarm" in data: target_plan.use_alarm = data["useAlarm"]
            target_plan.save()

            if old_taken_at and target_plan.regihistory:
                siblings = Plan.objects.filter(
                    regihistory=target_plan.regihistory,
                    taken_at=old_taken_at,
                    updated_at=old_updated_at
                ).exclude(id=target_plan.id)

                siblings.update(
                    taken_at=new_taken_at,
                    updated_at=target_plan.updated_at
                )
        else:
            if "medName" in data: target_plan.med_name = data["medName"]
            if "useAlarm" in data: target_plan.use_alarm = data["useAlarm"]
            target_plan.save()

        return Response(PlanSerializer(target_plan).data, status=status.HTTP_200_OK)


# =========================================================
# ⭐ [수정] 복약 완료 (일괄 처리 로직 추가)
# =========================================================
class MarkAsTakenView(APIView):
    """
    해당 Plan 및 같은 그룹(같은 처방, 같은 시간)의 모든 Plan을 '복약 완료' 처리합니다.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request, plan_id):
        # 1. 타겟 Plan 찾기
        target_plan = get_object_or_404(Plan, id=plan_id)

        # 2. 이미 먹었는지 확인
        if target_plan.taken:
            return Response({"message": "이미 복약 완료된 약입니다."}, status=status.HTTP_200_OK)

        now = timezone.now()

        # 3. [핵심] 같은 RegiHistory이면서 같은 시간(taken_at)인 약들을 모두 찾음 (아직 안 먹은 것만)
        siblings = Plan.objects.filter(
            regihistory=target_plan.regihistory,
            taken_at=target_plan.taken_at,
            taken__isnull=True
        )

        # 4. 일괄 업데이트 (타겟 포함)
        count = siblings.update(taken=now)

        # 만약 siblings 필터에서 target_plan이 포함되지 않는 구조라면 명시적으로 저장
        if not target_plan.taken:
            target_plan.taken = now
            target_plan.save()

        return Response({
            "message": f"총 {count}개의 약이 복약 완료 처리되었습니다.",
            "taken_time": now
        }, status=status.HTTP_200_OK)


# =========================================================
# ⭐ [수정] 복약 미루기 (일괄 처리 로직 추가)
# =========================================================
class SnoozeMedicationView(APIView):
    """
    해당 Plan 및 같은 그룹의 복용 예정 시간(taken_at)을 30분 뒤로 미룹니다.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request, plan_id):
        target_plan = get_object_or_404(Plan, id=plan_id)

        if target_plan.taken:
            return Response({"error": "이미 복약한 약입니다."}, status=status.HTTP_400_BAD_REQUEST)

        # 30분 추가
        new_time = target_plan.taken_at + datetime.timedelta(minutes=30)

        # [핵심] 같은 RegiHistory이면서 같은 시간인 모든 약을 찾음
        siblings = Plan.objects.filter(
            regihistory=target_plan.regihistory,
            taken_at=target_plan.taken_at
        )

        # 일괄 업데이트
        count = siblings.update(taken_at=new_time)

        return Response({
            "message": f"총 {count}개의 알림이 30분 뒤로 미루어졌습니다.",
            "new_taken_at": new_time
        }, status=status.HTTP_200_OK)