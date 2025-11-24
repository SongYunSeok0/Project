from rest_framework import serializers
from .models import HeartRate, StepCount, DailyStep
import datetime
from django.utils import timezone


class HeartRateSerializer(serializers.ModelSerializer):
    class Meta:
        model = HeartRate
        fields = '__all__'
        read_only_fields = ["id", "user"]


class StepCountSerializer(serializers.ModelSerializer):
    collected_at = serializers.IntegerField(write_only=True)
    collected_at_dt = serializers.DateTimeField(read_only=True, source='collected_at')

    class Meta:
        model = StepCount
        fields = ['steps', 'collected_at', 'collected_at_dt', 'user']
        read_only_fields = ['user', 'collected_at_dt']

    def create(self, validated_data):
        ts = validated_data.pop('collected_at')
        dt_naive = datetime.datetime.fromtimestamp(ts / 1000)
        dt = timezone.make_aware(dt_naive)
        user = self.context['request'].user
        stepcount, created = StepCount.objects.update_or_create(
            user=user,
            collected_at=dt,
            defaults={'steps': validated_data['steps']}
        )
        return stepcount



class DailyStepSerializer(serializers.ModelSerializer):
    class Meta:
        model = DailyStep
        fields = '__all__'
        read_only_fields = ["id", "user"]
