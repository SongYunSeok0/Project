from rest_framework import serializers
from .models import Prescription, Plan
from django.utils import timezone
import datetime


# ----------------------------
#  공통 함수: timestamp <-> datetime 변환
# ----------------------------
def to_ms(dt):
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(dt, datetime.time.min, tzinfo=timezone.get_current_timezone())
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


def from_ms(ms):
    if ms in (None, ""):
        return None
    return datetime.datetime.utcfromtimestamp(int(ms) / 1000).date()


# ----------------------------
#  스케줄 시간 (PlanTimeSerializer)
# ----------------------------
class PlanTimeSerializer(serializers.Serializer):
    """복용 시간 및 요일"""
    alarm_time = serializers.CharField()  # ex: "09:00"
    days_of_week = serializers.ListField(
        child=serializers.IntegerField(min_value=0, max_value=6),
        default=list,
        required=False
    )


# ----------------------------
#  복용 스케줄 조회용 (MedicationPlan)
# ----------------------------
class PlanSerializer(serializers.ModelSerializer):
    """MedicationPlan 조회용"""
    userId = serializers.SerializerMethodField()
    startDay = serializers.SerializerMethodField()
    endDay = serializers.SerializerMethodField()
    createdAt = serializers.SerializerMethodField()
    updatedAt = serializers.SerializerMethodField()

    class Meta:
        model = Plan
        fields = [
            "userId",
            "prescriptionId",
            "medName",
            "takenAt",
            "mealTime",
            "note",
            "taken",
        ]

    def get_userId(self, obj):
        return str(obj.user_id)

    def get_startDay(self, obj):
        return to_ms(obj.start_date)

    def get_endDay(self, obj):
        return to_ms(obj.end_date)

    def get_createdAt(self, obj):
        return to_ms(obj.start_date)

    def get_updatedAt(self, obj):
        return to_ms(obj.end_date)


# ----------------------------
#  복용 스케줄 생성 입력용 (PlanCreateIn)
# ----------------------------
class PlanTimeIn(serializers.Serializer):
    """단일 시간 정보"""
    alarm_time = serializers.CharField(required=True)  # "HH:MM" 형식
    days_of_week = serializers.ListField(
        child=serializers.IntegerField(min_value=0, max_value=6),
        required=False,
        default=list,
    )


class PlanCreateIn(serializers.Serializer):
    prescriptionId = serializers.IntegerField(required=False, allow_null=True)
    medName = serializers.CharField()
    takenAt = serializers.IntegerField(required=True)
    mealTime = serializers.CharField(required=False, allow_null=True)
    note = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    taken = serializers.IntegerField(required=False, allow_null=True)

    def to_dt(self, ms):
        if ms is None:
            return None
        return datetime.datetime.fromtimestamp(ms / 1000, tz=timezone.get_current_timezone())

