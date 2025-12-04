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


# ✅ [수정] PlanListView (GET: 조회, POST: 단건 등록 + 스마트 일괄 등록 통합)
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = Plan.objects.filter(regihistory__user=request.user)
        return Response(PlanSerializer(plans, many=True).data, status=status.HTTP_200_OK)

    def post(self, request):
        data = request.data

        # =================================================================
        # [Case 1] 스마트 일괄 등록
        # =================================================================
        if "times" in data and isinstance(data["times"], list):
            rid = data.get("regihistoryId")
            start_date_str = data.get("startDate")
            duration = int(data.get("duration", 1))
            times = data.get("times", [])
            med_name = data.get("medName", "")

            regi = RegiHistory.objects.filter(id=rid, user=request.user).first()
            if not regi:
                return Response({"error": "RegiHistory not found"}, status=404)

            # 날짜 파싱
            try:
                current_date = datetime.datetime.strptime(start_date_str, "%Y-%m-%d").date()
            except:
                print(f"[Plan Create] 날짜 파싱 실패 또는 없음: {start_date_str}, 오늘 날짜로 대체합니다.")
                current_date = timezone.now().date()

            now = timezone.now()
            print(f"✅ [DEBUG] 서버 현재 시간(now): {now} (Timezone: {timezone.get_current_timezone()})")

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
                        # 날짜 + 시간 결합
                        plan_dt = datetime.datetime.combine(current_date, datetime.time(hour, minute))

                        # Timezone 처리 (Asia/Seoul 등으로 변환)
                        if timezone.is_naive(plan_dt):
                            plan_dt = timezone.make_aware(plan_dt, timezone.get_current_timezone())

                        # 디버깅용 로그
                        # print(f"👉 [Check] {plan_dt} > {now} ? {plan_dt > now}")

                        # ⭐ [핵심 로직] 현재 시간보다 미래인 경우에만 생성
                        if plan_dt > now:
                            p = Plan.objects.create(
                                regihistory=regi,
                                med_name=med_name,
                                taken_at=plan_dt,
                                ex_taken_at=plan_dt,  # 👈 추가: 최초 예정 시간 기록
                                use_alarm=True,
                                meal_time="after"
                            )
                            created_plans.append(p)
                            created_count += 1
                        else:
                            # 이미 지난 시간은 스킵 (로그 확인용)
                            print(f"⏭️ [SKIP] 과거 시간 스킵됨: {plan_dt}")

                    except Exception as e:
                        print(f"⚠️ [ERROR] 시간 처리 중 오류: {e}")
                        continue

                current_date += datetime.timedelta(days=1)
                days_looped += 1

            # created_at/updated_at 그룹화 (생략 가능하나 유지)
            if created_plans:
                sync_time = timezone.now()
                Plan.objects.filter(id__in=[p.id for p in created_plans]).update(updated_at=sync_time)
                for p in created_plans:
                    p.updated_at = sync_time

            return Response({
                "message": f"총 {created_count}개의 스마트 일정이 생성되었습니다.",
                "plans": PlanSerializer(created_plans, many=True).data
            }, status=status.HTTP_201_CREATED)

        # =================================================================
        # [Case 2] 기존 단건 등록 (변동 없음)
        # =================================================================
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
                ex_taken_at=taken_at_value,  # 👈 추가: 최초 예정 시간 기록
                meal_time=v.get("mealTime") or "before",
                note=v.get("note"),
                taken=to_dt(v.get("taken")),
                use_alarm=v.get("useAlarm", True),
            )

            return Response(PlanSerializer(plan).data, status=status.HTTP_201_CREATED)


# Plan GET + POST
# class PlanListView(APIView):
#     permission_classes = [IsAuthenticated]
#
#     def get(self, request):
#         plans = Plan.objects.filter(regihistory__user=request.user)
#         return Response(PlanSerializer(plans, many=True).data, status=status.HTTP_200_OK)
#
#     def post(self, request):
#         ser = PlanCreateIn(data=request.data)
#         ser.is_valid(raise_exception=True)
#         v = ser.validated_data
#
#         regi_history = None
#         rid = v.get("regihistoryId")
#         if rid is not None:
#             regi_history = RegiHistory.objects.filter(id=rid, user=request.user).first()
#             if regi_history is None:
#                 return Response({"error": "no permission"}, status=status.HTTP_400_BAD_REQUEST)
#
#         plan = Plan.objects.create(
#             regihistory=regi_history,
#             med_name=v.get("medName"),
#             taken_at=to_dt(v.get("takenAt")),
#             meal_time=v.get("mealTime") or "before",
#             note=v.get("note"),
#             taken=to_dt(v.get("taken")),
#             use_alarm=v.get("useAlarm", True),
#         )
#
#         return Response(PlanSerializer(plan).data, status=status.HTTP_201_CREATED)


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


# ✅ [수정됨] PlanUpdateView (업데이트 시간 동기화 로직 포함)
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
