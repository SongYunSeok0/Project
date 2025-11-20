from rest_framework import serializers
from django.utils import timezone
import datetime

from .models import RegiHistory, Plan


# ----------------------------
#  공통 함수: timestamp <-> datetime 변환
# ----------------------------
def to_ms(dt):
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(
            dt,
            datetime.time.min,
            tzinfo=timezone.get_current_timezone(),
        )
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


def from_ms(ms):
    if ms in (None, ""):
        return None
    return datetime.datetime.utcfromtimestamp(int(ms) / 1000.0)


# ----------------------------
#  (옵션) 단일 시간 정보
# ----------------------------
class PlanTimeSerializer(serializers.Serializer):
    alarm_time = serializers.CharField()  # "09:00"
    days_of_week = serializers.ListField(
        child=serializers.IntegerField(min_value=0, max_value=6),
        default=list,
        required=False,
    )


# ----------------------------
#  복용 스케줄 조회용
# ----------------------------
class PlanSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(read_only=True)
    regiHistoryId = serializers.SerializerMethodField()
    medName = serializers.CharField(source="med_name")
    takenAt = serializers.SerializerMethodField()
    mealTime = serializers.CharField(source="meal_time")
    taken = serializers.SerializerMethodField()
    createdAt = serializers.SerializerMethodField()
    updatedAt = serializers.SerializerMethodField()

    class Meta:
        model = Plan
        fields = [
            "id",
            "regiHistoryId",
            "medName",
            "takenAt",
            "mealTime",
            "note",
            "taken",
            "createdAt",
            "updatedAt",
        ]

    def get_regiHistoryId(self, obj):
        return obj.RegiHistory.id if obj.RegiHistory else None

    def get_takenAt(self, obj):
        return to_ms(obj.taken_at)

    def get_taken(self, obj):
        return to_ms(obj.taken)

    def get_createdAt(self, obj):
        return to_ms(obj.created_at)

    def get_updatedAt(self, obj):
        return to_ms(obj.updated_at)


# ----------------------------
#  복용 스케줄 생성 입력용
# ----------------------------
class PlanTimeIn(serializers.Serializer):
    alarm_time = serializers.CharField(required=True)  # "HH:MM"
    days_of_week = serializers.ListField(
        child=serializers.IntegerField(min_value=0, max_value=6),
        required=False,
        default=list,
    )


class PlanCreateIn(serializers.Serializer):
    # ✅ 이제 처방전이 아니라 RegiHistory FK 사용
    regiHistoryId = serializers.IntegerField(required=False, allow_null=True)

    medName = serializers.CharField()
    takenAt = serializers.IntegerField(required=False, allow_null=True)  # ms
    mealTime = serializers.CharField(required=False, allow_null=True)
    note = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    taken = serializers.IntegerField(required=False, allow_null=True)  # ms

    def to_dt(self, ms):
        if ms is None:
            return None
        return datetime.datetime.fromtimestamp(
            ms / 1000.0, tz=timezone.get_current_timezone()
        )
