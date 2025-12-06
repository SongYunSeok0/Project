import secrets
from django.db import models
from django.conf import settings

class IntakeStatus(models.TextChoices):
    TAKEN = "taken", "정상 복용"
    WRONG = "wrong", "오복용"
    MISSED = "missed", "미복용"
    NONE = "none", "이벤트 아님"


def generate_device_token():
    """64글자짜리 안전한 랜덤 토큰 생성"""
    return secrets.token_hex(32)  # 32바이트 → 64 hex 문자열


def generate_device_uuid():
    """디바이스 고유 UUID 생성"""
    return secrets.token_hex(8)  # 16 hex 문자열 (간단한 UUID)


class Device(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="iot_devices",
    )

    device_uuid = models.CharField(max_length=64, unique=True, default=generate_device_uuid)
    device_token = models.CharField(max_length=128, default=generate_device_token)

    is_active = models.BooleanField(default=True)
    last_connected_at = models.DateTimeField(null=True, blank=True)
    device_name=models.CharField(
        max_length=10,
        null=True,
        blank=True
    )


    class Meta:
        db_table = "iot_device"
        unique_together = ('user', 'device_name')

    def __str__(self):
        return f"Device {self.device_uuid} for {self.user.email}"

class SensorData(models.Model):
    device = models.ForeignKey(
        Device, on_delete=models.CASCADE, related_name="sensor_data"
    )
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="sensor_data"
    )
    is_opened = models.BooleanField(default=False)  # 약통 개봉 여부
    is_time = models.BooleanField(default=False)    # 복용 시간대 여부
    collected_at = models.DateTimeField()           # 측정된 실제 시각 (디바이스 기준)
    created_at = models.DateTimeField(auto_now_add=True)  # 서버 수집 시각

    status = models.CharField(
        max_length=10,
        choices=IntakeStatus.choices,
        default=IntakeStatus.NONE
    )

    class Meta:
        db_table = "iot_sensordata"
        ordering = ["-collected_at"]

    def __str__(self):
        return f"SensorData {self.device.id} @ {self.collected_at}"


import secrets
from django.db import models



class IntakeStatus(models.TextChoices):
    TAKEN = "taken", "정상 복용"
    WRONG = "wrong", "오복용"
    MISSED = "missed", "미복용"
    NONE = "none", "이벤트 아님"
