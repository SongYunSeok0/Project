# medications/serializers.py
from rest_framework import serializers
from .models import Medication
from django.utils import timezone
import datetime

class PlanTimeSerializer(serializers.Serializer):
    time = serializers.TimeField()
    days_of_week = serializers.ListField(child=serializers.IntegerField(), default=list)

class PlanSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    userId = serializers.CharField()
    type = serializers.ChoiceField(choices=["DISEASE", "SUPPLEMENT"])
    diseaseName = serializers.CharField(allow_null=True)
    supplementName = serializers.CharField(allow_null=True)
    dosePerDay = serializers.IntegerField()
    mealRelation = serializers.ChoiceField(choices=["BEFORE", "AFTER", "NONE"], allow_null=True)
    memo = serializers.CharField(allow_null=True)
    startDay = serializers.IntegerField()   # epoch ms
    endDay = serializers.IntegerField(allow_null=True)
    createdAt = serializers.IntegerField()
    updatedAt = serializers.IntegerField()
    times = PlanTimeSerializer(many=True)

    @staticmethod
    def _to_ms(dt):
        if dt is None:
            return None
        if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
            dt = datetime.datetime.combine(dt, datetime.time.min, tzinfo=timezone.get_current_timezone())
        if timezone.is_naive(dt):
            dt = timezone.make_aware(dt, timezone.get_current_timezone())
        return int(dt.timestamp() * 1000)

    @classmethod
    def from_medication(cls, m: Medication) -> dict:
        scheds = list(m.medicationschedule_set.filter(is_active=True).order_by("time"))
        return {
            "id": m.id,
            "userId": str(m.user_id),
            "type": "SUPPLEMENT",                 # 규칙 없으니 기본 SUPPLEMENT
            "diseaseName": None,
            "supplementName": m.name,
            "dosePerDay": len(scheds),
            "mealRelation": "NONE",               # 서버 모델에 식사 연관 없음 → NONE
            "memo": m.note or None,
            "startDay": cls._to_ms(m.start_date),
            "endDay": cls._to_ms(m.end_date),
            "createdAt": cls._to_ms(m.created_at),
            "updatedAt": cls._to_ms(m.updated_at),
            "times": [{"time": s.time, "days_of_week": s.days_of_week or []} for s in scheds],
        }
class PlanTimeIn(serializers.Serializer):
    time = serializers.CharField(required=True)                 # "HH:MM" 또는 "HH:MM:SS"
    days_of_week = serializers.ListField(
        child=serializers.IntegerField(min_value=0, max_value=6), required=False, default=list
    )

    def to_python_time(self, s: str) -> datetime.time:
        # "HH:MM" 지원
        try:
            return datetime.time.fromisoformat(s)
        except ValueError:
            hh, mm = s.split(":")
            return datetime.time(int(hh), int(mm), 0)

class PlanCreateIn(serializers.Serializer):
    supplementName = serializers.CharField(required=True)
    memo = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    startDay = serializers.IntegerField(required=False, allow_null=True)  # epoch ms
    endDay = serializers.IntegerField(required=False, allow_null=True)
    times = PlanTimeIn(many=True, required=False, default=list)

    def to_date(self, ms):
        if ms in (None, ""):
            return None
        return datetime.datetime.utcfromtimestamp(int(ms)/1000).date()