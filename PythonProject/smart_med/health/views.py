from rest_framework import viewsets, permissions
from .models import HeartRate, StepCount
from .serializers import HeartRateSerializer, StepCountSerializer


# 심박수 API
class HeartRateViewSet(viewsets.ModelViewSet):
    queryset = HeartRate.objects.all()
    serializer_class = HeartRateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        # 로그인한 사용자 자신의 데이터만 조회
        return HeartRate.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


# 걸음수 API
class StepCountViewSet(viewsets.ModelViewSet):
    queryset = StepCount.objects.all()
    serializer_class = StepCountSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return StepCount.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
