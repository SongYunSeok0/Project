# medications/views.py

from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated, BasePermission
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .docs import mark_as_taken_docs, snooze_docs
import datetime

# 서비스 및 시리얼라이저 임포트
from . import services
from .serializers import (
    RegiHistorySerializer, RegiHistoryCreateSerializer, RegiHistoryWithPlansSerializer,
    PlanSerializer, PlanCreateInputSerializer
)
from .models import RegiHistory, Plan
from medications.tasks import delete_plan_async

# 문서화 데코레이터들 (기존 유지)
from .docs import (
    regi_list_docs, regi_create_docs, regi_update_docs, regi_delete_docs,
    plan_list_docs, plan_create_docs, plan_delete_docs,
    plan_today_docs, plan_update_docs
)


class IsStaffUser(BasePermission):
    def has_permission(self, request, view):
        return bool(request.user and request.user.is_authenticated and request.user.is_staff)


# ==========================
# RegiHistory Views
# ==========================

@regi_list_docs
class RegiHistoryListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    @regi_create_docs
    def post(self, request):
        serializer = RegiHistoryCreateSerializer(
            data=request.data,
            context={"request": request}
        )
        serializer.is_valid(raise_exception=True)
        obj = serializer.save()
        return Response(RegiHistorySerializer(obj).data, status=201)

    def get(self, request):
        rows = RegiHistory.objects.filter(user=request.user).order_by("-id")
        return Response(RegiHistorySerializer(rows, many=True).data)


@regi_update_docs
class RegiHistoryUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        # 쿼리셋에서 바로 404 처리와 필터링을 동시에
        try:
            obj = RegiHistory.objects.get(id=pk, user=request.user)
        except RegiHistory.DoesNotExist:
            return Response({"error": "not found"}, status=404)

        serializer = RegiHistoryCreateSerializer(obj, data=request.data, partial=True, context={"request": request})
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(RegiHistorySerializer(obj).data)


@regi_delete_docs
class RegiHistoryDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        try:
            row = RegiHistory.objects.get(id=pk, user=request.user)
            row.delete()
            return Response(status=204)
        except RegiHistory.DoesNotExist:
            return Response({"error": "not found"}, status=404)


# ==========================
# Plan Views
# ==========================

@plan_list_docs
class PlanListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = Plan.objects.filter(regihistory__user=request.user).order_by('taken_at')
        return Response(PlanSerializer(plans, many=True).data)

    @plan_create_docs
    def post(self, request):
        data = request.data

        # 1. 스마트 일정 일괄 등록 (times 배열이 있는 경우)
        if "times" in data and isinstance(data["times"], list):
            try:
                created_plans = services.create_smart_schedule(request.user, data)
                return Response({
                    "message": f"{len(created_plans)}개의 스마트 일정이 생성되었습니다.",
                    "plans": PlanSerializer(created_plans, many=True).data
                }, status=201)
            except Exception as e:
                # 404나 기타 에러 처리
                return Response({"error": str(e)}, status=400)

        # 2. 단건 등록
        input_ser = PlanCreateInputSerializer(data=data)
        input_ser.is_valid(raise_exception=True)

        try:
            plan = services.create_single_plan(request.user, input_ser.validated_data)
            return Response(PlanSerializer(plan).data, status=201)
        except Exception as e:
            return Response({"error": str(e)}, status=400)


@plan_delete_docs
class PlanDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        # 존재 여부 확인 (권한 체크 포함)
        if not Plan.objects.filter(id=pk, regihistory__user=request.user).exists():
            return Response({"error": "not found"}, status=404)

        # 비동기 삭제 태스크 실행
        delete_plan_async.delay(pk)
        return Response({"status": "delete queued"}, status=202)


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

        # 상태 계산 로직은 Serializer에 위임되어 있으므로 그대로 호출
        return Response(PlanSerializer(plans, many=True).data)


@plan_update_docs
class PlanUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        try:
            updated_plan = services.update_plan_time(request.user, pk, request.data)
            return Response(PlanSerializer(updated_plan).data)
        except Exception as e:
            # get_object_or_404에서 발생하는 404 에러 등 처리
            return Response({"error": str(e)}, status=400)

class UserRegiHistoryListView(APIView):
    """
    특정 사용자(user_id)의 등록 이력 + Plan 목록
    """
    permission_classes = [IsStaffUser]

    def get(self, request, user_id):
        rows = (
            RegiHistory.objects
            .filter(user_id=user_id)
            .order_by("-id")
        )
        return Response(RegiHistoryWithPlansSerializer(rows, many=True).data)


class AllRegiHistoryListView(APIView):
    """
    전체 사용자에 대한 등록 이력 + Plan 목록
    """
    permission_classes = [IsStaffUser]

    def get(self, request):
        rows = RegiHistory.objects.all().order_by("-id")
        return Response(RegiHistoryWithPlansSerializer(rows, many=True).data)

@mark_as_taken_docs
class MarkAsTakenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, plan_id):
        try:
            services.mark_plan_as_taken(request.user, plan_id)
            return Response({"status": "ok", "message": "복용 완료 처리되었습니다."}, status=200)
        except Exception as e:
            return Response({"error": str(e)}, status=400)


@snooze_docs
class SnoozeMedicationView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, plan_id):
        minutes = int(request.data.get("minutes", 10))
        try:
            services.snooze_plan(request.user, plan_id, minutes)
            return Response({"status": "ok", "message": f"{minutes}분 뒤로 알림을 미뤘습니다."}, status=200)
        except ValueError as e:
            # 이미 복용한 경우 등
            return Response({"error": str(e)}, status=400)
        except Exception as e:
            return Response({"error": str(e)}, status=500)