from rest_framework import serializers
from .models import HeartRate, StepCount

class HeartRateSerializer(serializers.ModelSerializer):
    class Meta:
        model = HeartRate
        fields = '__all__'

class StepCountSerializer(serializers.ModelSerializer):
    class Meta:
        model = StepCount
        fields = '__all__'
