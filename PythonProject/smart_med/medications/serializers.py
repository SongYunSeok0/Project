from rest_framework import serializers
from iot.models import Device
from datetime import timedelta
from django.utils import timezone

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
    takenTime = serializers.SerializerMethodField()
    useAlarm = serializers.BooleanField(source="use_alarm")

    status = serializers.SerializerMethodField()

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
            "takenTime",
            "useAlarm",
            "status",
        ]

    def get_regihistoryId(self, obj):
        return obj.regihistory.id if obj.regihistory else None

    def get_takenAt(self, obj):
        return to_ms(obj.taken_at)

    def get_exTakenAt(self, obj):
        return to_ms(obj.ex_taken_at)

    def get_taken(self, obj):
        return obj.taken is not None
    
    def get_takenTime(self, obj):
        return to_ms(obj.taken) if obj.taken else None
    
    def get_status(self, obj):
        # 1) 이미 복용 완료
        if obj.taken:
            return "done"

        # taken_at 이 없으면 그냥 예정 상태로 둠
        if not obj.taken_at:
            return "pending"

        now = timezone.now()
        # 2) 복용 예정 시간 + 30분이 지났는데 taken 이 없으면 → 미복용
        if now > obj.taken_at + timedelta(minutes=30):
            return "missed"

        # 3) 그 외는 아직 예정 상태
        return "pending"


#  Plan 생성용 입력 Serializer (Raw 입력)
class PlanCreateIn(serializers.Serializer):
    regihistoryId = serializers.IntegerField(required=True, allow_null=False)
    medName = serializers.CharField()
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

        # 영양제이면 med_name을 비워둔다
        if regi.regi_type == "supplement":
            validated_data.pop("med_name", None)

        return Plan.objects.create(regihistory=regi, **validated_data)


#  RegiHistory 조회용 Serializer (일반 사용자용)
class RegiHistorySerializer(serializers.ModelSerializer):
    userId = serializers.IntegerField(source="user.id", read_only=True)
    useAlarm = serializers.BooleanField(source="use_alarm")
    label = serializers.SerializerMethodField()

    class Meta:
        model = RegiHistory
        fields = [
            "id",
            "userId",
            "regi_type",
            "label",
            "issued_date",
            "useAlarm",
            "device",
        ]
    def get_label(self, obj):
        return obj.label or "복약 기록"


#  RegiHistory 생성 Serializer
class RegiHistoryCreateSerializer(serializers.ModelSerializer):
    useAlarm = serializers.BooleanField(
        source="use_alarm",
        required=False,
        default=True
    )

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


#  관리자용: RegiHistory + Plan 목록 응답 Serializer
class RegiHistoryWithPlansSerializer(serializers.ModelSerializer):
    # user: Long (JSON key: "user")
    user = serializers.IntegerField(source="user.id", read_only=True)
    # use_alarm: Boolean (JSON key: "use_alarm")
    use_alarm = serializers.BooleanField()
    # plans: List<PlanSerializer> (JSON key: "plans")
    plans = PlanSerializer(many=True, read_only=True, source="plan_set")
    # plan_count: Int (JSON key: "plan_count")
    plan_count = serializers.SerializerMethodField()
    label = serializers.SerializerMethodField()

    class Meta:
        model = RegiHistory
        fields = [
            "id",
            "user",
            "regi_type",
            "label",
            "issued_date",
            "use_alarm",
            "device",
            "plan_count",
            "plans",
        ]

    def get_label(self, obj):
        return obj.label or "복약 기록"

    def get_plan_count(self, obj):
        # related_name 을 따로 안 줬으면 기본 reverse 이름이 plan_set
        return obj.plan_set.count()
