from rest_framework import serializers
from django.utils import timezone
import datetime

from .models import RegiHistory, Plan


# ============================================================
#  공통 datetime ↔ ms
# ============================================================
def to_ms(dt):
    """datetime → ms"""
    if dt is None:
        return None

    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(
            dt,
            datetime.time.min,
            tzinfo=timezone.get_current_timezone()
        )

    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())

    return int(dt.timestamp() * 1000)


def from_ms(ms):
    """ms → datetime"""
    if ms in (None, "", 0):
        return None

    return datetime.datetime.fromtimestamp(
        ms / 1000.0,
        tz=timezone.get_current_timezone(),
    )


# ============================================================
#   Plan 조회용 Serializer
# ============================================================
class PlanSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(read_only=True)

    regiHistoryId = serializers.SerializerMethodField()
    medName = serializers.CharField(source="med_name")
    takenAt = serializers.SerializerMethodField()
    mealTime = serializers.CharField(source="meal_time")
    taken = serializers.SerializerMethodField()

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
        ]

    def get_regiHistoryId(self, obj):
        return obj.regihistory.id if obj.regihistory else None

    def get_takenAt(self, obj):
        return to_ms(obj.taken_at)

    def get_taken(self, obj):
        return to_ms(obj.taken)

# ============================================================
#   Plan 생성용 입력 Serializer
# ============================================================
class PlanCreateIn(serializers.Serializer):
    regiHistoryId = serializers.IntegerField(required=False, allow_null=True)

    medName = serializers.CharField()
    takenAt = serializers.IntegerField(required=False, allow_null=True)
    mealTime = serializers.CharField(required=False, allow_null=True)
    note = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    taken = serializers.IntegerField(required=False, allow_null=True)


# ============================================================
#   RegiHistory 조회 / 생성
# ============================================================
class RegiHistorySerializer(serializers.ModelSerializer):
    userId = serializers.IntegerField(source="user.id", read_only=True)

    class Meta:
        model = RegiHistory
        fields = [
            "id",
            "userId",
            "regi_type",
            "label",
            "issued_date",
        ]


class RegiHistoryCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = RegiHistory
        fields = [
            "regi_type",
            "label",
            "issued_date",
        ]

    def create(self, validated_data):
        user = self.context["request"].user
        return RegiHistory.objects.create(user=user, **validated_data)
