# health/views.py
from rest_framework import viewsets, permissions
from rest_framework.response import Response
from rest_framework.decorators import action

from .models import HeartRate, DailyStep
from .serializers import HeartRateSerializer, DailyStepSerializer
from .docs import (
    heart_rate_list_docs, heart_rate_latest_docs, heart_rate_create_docs,
    daily_step_list_docs, daily_step_create_docs
)

# ===========================================
# Heart Rate
# ===========================================

@heart_rate_list_docs
class HeartRateViewSet(viewsets.ModelViewSet):
    queryset = HeartRate.objects.all()
    serializer_class = HeartRateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return HeartRate.objects.filter(user=self.request.user).order_by(
            "-collected_at", "-id"
        )

    @heart_rate_latest_docs
    @action(detail=False, methods=["get"])
    def latest(self, request):
        qs = self.get_queryset().order_by("-collected_at", "-id")
        obj = qs.first()

        if not obj:
            return Response({"bpm": None, "collected_at": None})

        return Response(
            {"bpm": obj.bpm, "collected_at": obj.collected_at}
        )

    @heart_rate_create_docs
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


# ===========================================
# Daily Step
# ===========================================

@daily_step_list_docs
class DailyStepViewSet(viewsets.ModelViewSet):
    queryset = DailyStep.objects.all()
    serializer_class = DailyStepSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return DailyStep.objects.filter(user=self.request.user)

    @daily_step_create_docs
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
