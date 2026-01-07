# medications/serializers.py

from rest_framework import serializers
from django.utils import timezone
from datetime import timedelta
from .models import RegiHistory, Plan
from iot.models import Device
from smart_med.utils.time_utils import to_ms, from_ms


# ==========================
# Plan Serializers
# ==========================

class PlanSerializer(serializers.ModelSerializer):
    """조회용: SerializerMethodField 최소화 및 최적화"""
    id = serializers.IntegerField(read_only=True)
    regihistoryId = serializers.IntegerField(source="regihistory.id", read_only=True)
    regihistory_label = serializers.CharField(source="regihistory.label", read_only=True, default=None)
    medName = serializers.CharField(source="med_name")
    takenAt = serializers.SerializerMethodField()
    exTakenAt = serializers.SerializerMethodField()
    mealTime = serializers.CharField(source="meal_time")
    taken = serializers.SerializerMethodField()
    takenTime = serializers.SerializerMethodField()
    useAlarm = serializers.BooleanField(source="use_alarm")
    status = serializers.SerializerMethodField()

    class Meta:
        model = Plan
        fields = [
            "id", "regihistoryId", "regihistory_label", "medName",
            "takenAt", "exTakenAt", "mealTime", "note",
            "taken", "takenTime", "useAlarm", "status",
        ]

    def get_takenAt(self, obj):
        return to_ms(obj.taken_at)

    def get_exTakenAt(self, obj):
        return to_ms(obj.ex_taken_at)

    def get_taken(self, obj):
        return obj.taken is not None

    def get_takenTime(self, obj):
        return to_ms(obj.taken) if obj.taken else None

    def get_status(self, obj):
        # 1. 이미 복용함
        if obj.taken:
            return "done"

        # 2. taken_at 없음 (예외)
        if not obj.taken_at:
            return "pending"

        now = timezone.now()
        # 3. 예정 시간 30분 경과 -> 미복용(missed)
        if now > obj.taken_at + timedelta(minutes=30):
            return "missed"

        # 4. 그 외 -> 예정(pending)
        return "pending"


class PlanCreateInputSerializer(serializers.Serializer):
    """입력 검증용 (DB 생성 로직 없음)"""
    regihistoryId = serializers.IntegerField(required=True)
    medName = serializers.CharField(required=False, allow_blank=True)
    takenAt = serializers.IntegerField(required=True)  # ms 단위
    mealTime = serializers.CharField(required=False, allow_null=True)
    note = serializers.CharField(required=False, allow_blank=True)
    taken = serializers.IntegerField(required=False, allow_null=True)
    useAlarm = serializers.BooleanField(required=False, default=True)


# ==========================
# RegiHistory Serializers
# ==========================

class RegiHistorySerializer(serializers.ModelSerializer):
    userId = serializers.IntegerField(source="user.id", read_only=True)
    useAlarm = serializers.BooleanField(source="use_alarm")
    label = serializers.CharField(default="복약 기록")  # 모델에서 default처리 되어있으면 바로 써도 됨

    class Meta:
        model = RegiHistory
        fields = ["id", "userId", "regi_type", "label", "issued_date", "useAlarm", "device"]


class RegiHistoryCreateSerializer(serializers.ModelSerializer):
    useAlarm = serializers.BooleanField(source="use_alarm", default=True)
    device = serializers.PrimaryKeyRelatedField(
        queryset=Device.objects.all(),
        required=False,
        allow_null=True
    )

    class Meta:
        model = RegiHistory
        fields = ["regi_type", "label", "issued_date", "useAlarm", "device"]

    def validate_device(self, value):
        # 내 기기인지 확인
        user = self.context['request'].user
        if value and value.user != user:
            raise serializers.ValidationError("본인의 기기만 등록할 수 있습니다.")
        return value

    def create(self, validated_data):
        user = self.context['request'].user
        return RegiHistory.objects.create(user=user, **validated_data)


class RegiHistoryWithPlansSerializer(serializers.ModelSerializer):
    """관리자용: Plan 포함"""
    user = serializers.IntegerField(source="user.id", read_only=True)
    use_alarm = serializers.BooleanField()
    plans = PlanSerializer(many=True, read_only=True, source="plan_set")
    plan_count = serializers.IntegerField(source="plan_set.count", read_only=True)
    label = serializers.CharField(default="복약 기록")

    class Meta:
        model = RegiHistory
        fields = [
            "id", "user", "regi_type", "label", "issued_date",
            "use_alarm", "device", "plan_count", "plans"
        ]