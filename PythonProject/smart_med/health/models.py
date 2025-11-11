# health/models.py
from django.db import models
from django.conf import settings


class HeartRate(models.Model):
    """심박수 데이터"""
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="heart_rates",
        verbose_name="사용자"
    )
    bpm = models.PositiveSmallIntegerField("심박수 (bpm)")
    collected_at = models.DateTimeField("측정된 실제 시각")
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="서버 수집 시각")

    class Meta:
        db_table = "health_heartrate"
        ordering = ["-collected_at"]
        verbose_name = "심박수 데이터"
        verbose_name_plural = "심박수 데이터"

    def __str__(self):
        return f"{self.user.username} - {self.bpm} bpm ({self.collected_at:%Y-%m-%d %H:%M:%S})"


class StepCount(models.Model):
    """걸음 수 데이터"""
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="step_counts",
        verbose_name="사용자"
    )
    steps = models.PositiveIntegerField("걸음 수")
    collected_at = models.DateTimeField("측정된 실제 시각")
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="서버 수집 시각")

    class Meta:
        db_table = "health_stepcount"
        ordering = ["-collected_at"]
        verbose_name = "걸음 수 데이터"
        verbose_name_plural = "걸음 수 데이터"

    def __str__(self):
        return f"{self.user.username} - {self.steps} steps ({self.collected_at:%Y-%m-%d %H:%M:%S})"
