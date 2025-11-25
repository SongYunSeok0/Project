from rest_framework import serializers
from django.utils import timezone
import datetime

from .models import RegiHistory, Plan

#  공통 함수: timestamp <-> datetime 변환
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


#   Plan 조회용 Serializer
class PlanSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(read_only=True)
    regihistoryId = serializers.SerializerMethodField()
    medName = serializers.CharField(source="med_name")
    takenAt = serializers.SerializerMethodField()
    mealTime = serializers.CharField(source="meal_time")
    taken = serializers.SerializerMethodField()

    # 개별 알람 여부 추가
    useAlarm = serializers.BooleanField(source="use_alarm")

    class Meta:
        model = Plan
        fields = [
            "id",
            "regihistoryId",
            "medName",
            "takenAt",
            "mealTime",
            "note",
            "taken",
            "useAlarm",   # ← 추가됨
        ]

    def get_regihistoryId(self, obj):
        return obj.regihistory.id if obj.regihistory else None

    def get_takenAt(self, obj):
        return to_ms(obj.taken_at)

    def get_taken(self, obj):
        return to_ms(obj.taken)


#   Plan 생성용 입력 Serializer
class PlanCreateIn(serializers.Serializer):

    regihistoryId = serializers.IntegerField(required=False, allow_null=True)

    medName = serializers.CharField()
    takenAt = serializers.IntegerField(required=False, allow_null=True)
    mealTime = serializers.CharField(required=False, allow_null=True)
    note = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    taken = serializers.IntegerField(required=False, allow_null=True)

    # 개별 알람 여부 추가
    useAlarm = serializers.BooleanField(required=False, default=True)


#   RegiHistory 조회 Serializer
class RegiHistorySerializer(serializers.ModelSerializer):
    userId = serializers.IntegerField(source="user.id", read_only=True)

    # 전체 알람 여부 추가
    useAlarm = serializers.BooleanField(source="use_alarm")

    class Meta:
        model = RegiHistory
        fields = [
            "id",
            "userId",
            "regi_type",
            "label",
            "issued_date",
            "useAlarm",
        ]


#   RegiHistory 생성 Serializer
class RegiHistoryCreateSerializer(serializers.ModelSerializer):

    # 전체 알람 여부 생성 시도 가능
    useAlarm = serializers.BooleanField(source="use_alarm", required=False, default=True)

    class Meta:
        model = RegiHistory
        fields = [
            "regi_type",
            "label",
            "issued_date",
            "useAlarm",
        ]

    def create(self, validated_data):
        user = self.context["request"].user
        return RegiHistory.objects.create(user=user, **validated_data)
