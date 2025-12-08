from rest_framework import serializers
from iot.models import Device
from django.utils import timezone
import datetime

from .models import RegiHistory, Plan
from smart_med.utils.time_utils import to_ms, from_ms


#  Plan 조회용 Serializer
class PlanSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(read_only=True)
    regihistoryId = serializers.SerializerMethodField()
    regihistory_label = serializers.CharField(
        source="regihistory.label",
        read_only=True,
        default=None
    )
    medName = serializers.CharField(source="med_name")
    takenAt = serializers.SerializerMethodField()
    exTakenAt = serializers.SerializerMethodField()
    mealTime = serializers.CharField(source="meal_time")
    taken = serializers.SerializerMethodField()
    useAlarm = serializers.BooleanField(source="use_alarm")

    class Meta:
        model = Plan
        fields = [
            "id",
            "regihistoryId",
            "regihistory_label",
            "medName",
            "takenAt",
            "exTakenAt",
            "mealTime",
            "note",
            "taken",
            "useAlarm",
        ]

    def get_regihistoryId(self, obj):
        return obj.regihistory.id if obj.regihistory else None

    def get_takenAt(self, obj):
        return to_ms(obj.taken_at)

    def get_exTakenAt(self, obj):
        return to_ms(obj.ex_taken_at)

    def get_taken(self, obj):
        return to_ms(obj.taken)


#  Plan 생성용 입력 Serializer (Raw 입력)
class PlanCreateIn(serializers.Serializer):
    # RegiHistory는 반드시 있어야 한다고 가정 → 필수
    regihistoryId = serializers.IntegerField(required=True, allow_null=False)

    medName = serializers.CharField()

    # 복용 예정 시간은 필수 (비워두면 400 발생)
    takenAt = serializers.IntegerField(required=True, allow_null=False)

    mealTime = serializers.CharField(required=False, allow_null=True)
    note = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    taken = serializers.IntegerField(required=False, allow_null=True)

    useAlarm = serializers.BooleanField(required=False, default=True)


#  Plan 생성 실제 수행 Serializer
class PlanCreateSerializer(serializers.ModelSerializer):
    regihistoryId = serializers.IntegerField(write_only=True)

    medName = serializers.CharField(
        source="med_name",
        required=False,
        allow_blank=True,
        allow_null=True,
    )

    takenAt = serializers.IntegerField(
        write_only=True,
        required=True,
        allow_null=False,
    )
    exTakenAt = serializers.IntegerField(
        write_only=True,
        required=False,
        allow_null=True
    )
    mealTime = serializers.CharField(
        source="meal_time",
        required=False,
        allow_null=True
    )
    note = serializers.CharField(
        required=False,
        allow_blank=True,
        allow_null=True
    )
    taken = serializers.IntegerField(
        write_only=True,
        required=False,
        allow_null=True
    )
    useAlarm = serializers.BooleanField(
        source="use_alarm",
        required=False,
        default=True
    )

    class Meta:
        model = Plan
        fields = [
            "regihistoryId",
            "medName",
            "takenAt",
            "exTakenAt",
            "mealTime",
            "note",
            "taken",
            "useAlarm",
        ]

    def create(self, validated_data):
        regihistory_id = validated_data.pop("regihistoryId")

        try:
            regi = RegiHistory.objects.get(id=regihistory_id)
        except RegiHistory.DoesNotExist:
            raise serializers.ValidationError({"regihistoryId": "RegiHistory not found"})

        taken_at_ms = validated_data.pop("takenAt", None)
        ex_taken_at_ms = validated_data.pop("exTakenAt", None)
        taken_ms = validated_data.pop("taken", None)

        if taken_at_ms is None:
            raise serializers.ValidationError({"takenAt": "복용 예정 시간은 필수입니다."})

        validated_data["taken_at"] = from_ms(taken_at_ms)

        if ex_taken_at_ms is not None:
            validated_data["ex_taken_at"] = from_ms(ex_taken_at_ms)
        if taken_ms is not None:
            validated_data["taken"] = from_ms(taken_ms)

        if regi.regi_type == "supplement":
            validated_data.pop("med_name", None)

        return Plan.objects.create(regihistory=regi, **validated_data)



#  RegiHistory 조회용 Serializer
class RegiHistorySerializer(serializers.ModelSerializer):
    userId = serializers.IntegerField(source="user.id", read_only=True)
    useAlarm = serializers.BooleanField(source="use_alarm")

    # device FK를 그대로 노출 (PK 값으로 직렬화됨) → JSON: "device": 3
    class Meta:
        model = RegiHistory
        fields = [
            "id",
            "userId",
            "regi_type",
            "label",
            "issued_date",
            "useAlarm",
            "device",   # JSON: "device": <device_pk>
        ]


#  RegiHistory 생성 Serializer
class RegiHistoryCreateSerializer(serializers.ModelSerializer):
    useAlarm = serializers.BooleanField(
        source="use_alarm",
        required=False,
        default=True
    )

    # 클라이언트에서 "device": 3 으로 보내면 받는 필드
    device = serializers.IntegerField(
        write_only=True,
        required=False,
        allow_null=True,
    )

    class Meta:
        model = RegiHistory
        fields = [
            "regi_type",
            "label",
            "issued_date",
            "useAlarm",
            "device",
        ]

    def create(self, validated_data):
        user = self.context["request"].user

        # device는 int PK로 들어옴 → 실제 Device FK로 매핑
        device_id = validated_data.pop("device", None)
        device = None
        if device_id is not None:
            device = Device.objects.filter(id=device_id, user=user).first()
            if device is None:
                raise serializers.ValidationError({"device": "유효하지 않은 기기입니다."})

        return RegiHistory.objects.create(
            user=user,
            device=device,
            **validated_data
        )
