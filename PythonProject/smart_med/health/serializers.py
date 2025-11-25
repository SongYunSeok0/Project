from rest_framework import serializers
from .models import HeartRate, DailyStep
import datetime
from django.utils import timezone


class HeartRateSerializer(serializers.ModelSerializer):
    class Meta:
        model = HeartRate
        fields = '__all__'
        read_only_fields = ["id", "user"]


class DailyStepSerializer(serializers.ModelSerializer):
    class Meta:
        model = DailyStep
        fields = '__all__'
        read_only_fields = ["id", "user"]
