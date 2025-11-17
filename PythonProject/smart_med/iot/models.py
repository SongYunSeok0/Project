from django.db import models
from django.conf import settings


class Device(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="iot_devices"
    )
    is_active = models.BooleanField(default=True)
    last_connected_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "iot_device"

    def __str__(self):
        return f"Device {self.id} for {self.user.email}"


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

    class Meta:
        db_table = "iot_sensordata"
        ordering = ["-collected_at"]

    def __str__(self):
        return f"SensorData {self.device.id} @ {self.collected_at}"
