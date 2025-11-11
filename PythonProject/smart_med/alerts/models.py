from django.db import models
from users.models import User

class Device(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    fcm_token = models.CharField(max_length=255, null=True, blank=True)


class SensorData(models.Model):
    is_opened = models.BooleanField(default=False)
    sensor_value = models.IntegerField(default=0)
    heart_rate = models.IntegerField(null=True, blank=True)
    timestamp = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-timestamp']

    def __str__(self):
        return f"Sensor {self.sensor_value} | Opened: {self.is_opened}"

