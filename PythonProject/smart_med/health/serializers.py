from rest_framework import serializers
from .models import HeartRate, StepCount

class HeartRateSerializer(serializers.ModelSerializer):
    class Meta:
        model = HeartRate
        fields = '__all__'
        read_only_fields = ["id", "user"]

class StepCountSerializer(serializers.ModelSerializer):
    class Meta:
        model = StepCount
        fields = '__all__'
        read_only_fields = ["id", "user"]
