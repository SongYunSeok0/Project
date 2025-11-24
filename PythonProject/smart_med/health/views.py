from rest_framework import viewsets, permissions
from .models import HeartRate, StepCount ,DailyStep
from .serializers import HeartRateSerializer, StepCountSerializer, DailyStepSerializer
from rest_framework.response import Response
from rest_framework.decorators import action


# 심박수 API
class HeartRateViewSet(viewsets.ModelViewSet):
    queryset = HeartRate.objects.all()
    serializer_class = HeartRateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        # 로그인한 사용자 자신의 데이터만 조회
        return HeartRate.objects\
        .filter(user=self.request.user)\
        .order_by("-collected_at", "-id")


    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    @action(detail=False, methods=["get"])
    def latest(self, request):
        qs = (
            self.get_queryset()
            .order_by("-collected_at", "-id")   # 가장 최근 데이터
        )
        obj = qs.first()
        if not obj:
            # 데이터 아직 없으면 null 리턴
            return Response({"bpm": None, "collected_at": None})

        # 그냥 기존 HeartRateSerializer 재사용해도 되고
        # serializer_data = self.get_serializer(obj).data
        return Response(
            {
                "bpm": obj.bpm,
                "collected_at": obj.collected_at,
            }
        )

# 걸음수 API
class StepCountViewSet(viewsets.ModelViewSet):
    queryset = StepCount.objects.all()
    serializer_class = StepCountSerializer

    def get_queryset(self):
        # 로그인 유저 기준
        return StepCount.objects.filter(user=self.request.user)

    def list(self, request, *args, **kwargs):
        # 1분 단위로 합산
        qs = self.get_queryset()
        aggregated = {}

        for sc in qs:
            # minute 단위 key
            minute_key = sc.collected_at.replace(second=0, microsecond=0)
            aggregated[minute_key] = aggregated.get(minute_key, 0) + sc.steps

        # 결과를 리스트 형태로 변환
        result = [{"minute": k, "steps": v} for k, v in aggregated.items()]
        result.sort(key=lambda x: x["minute"])

        return Response(result)

# 하루 총 걸음수 API
class DailyStepViewSet(viewsets.ModelViewSet):
    queryset = DailyStep.objects.all()
    serializer_class = DailyStepSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        # 로그인 유저의 일별 데이터만
        return DailyStep.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)