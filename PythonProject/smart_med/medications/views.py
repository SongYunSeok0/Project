from rest_framework import viewsets, permissions
from rest_framework.permissions import IsAuthenticated
from .models import Medication, MedicationSchedule, MedicationHistory
from .serializers import (MedicationSerializer,MedicationScheduleSerializer,MedicationHistorySerializer)


class MedicationViewSet(viewsets.ModelViewSet):
    queryset = Medication.objects.all()
    serializer_class = MedicationSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        # 로그인한 사용자만 자신의 약 조회 가능
        user = self.request.user
        return Medication.objects.filter(user=user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

class MedicationScheduleViewSet(viewsets.ModelViewSet):
    queryset = MedicationSchedule.objects.all()
    serializer_class = MedicationScheduleSerializer
    permission_classes = [permissions.IsAuthenticated]

class MedicationHistoryViewSet(viewsets.ModelViewSet):
    queryset = MedicationHistory.objects.all()
    serializer_class = MedicationHistorySerializer
    permission_classes = [permissions.IsAuthenticated]