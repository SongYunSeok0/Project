from django.db import models
from django.conf import settings

from iot.models import Device


# 등록 이력(기존 Prescription)
class RegiHistory(models.Model):
    # 사용자 ID (ForeignKey로 User 테이블과 연결 가능)

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        verbose_name="사용자 ID"
    )

    # 등록 유형 (영양제 / 병원약 등)
    regi_type = models.CharField(
        max_length=50,
        verbose_name="등록 유형 (영양제/병원약)"
    )

    device = models.ForeignKey(
        Device,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="regi_histories", # Device 입장에서 연결된 약들을 찾을 때 사용 (device.regi_histories.all())
        verbose_name="연동 기기"
    )

    # 병명 (기존 disase_name)
    label = models.CharField(
        max_length=100,
        verbose_name="병명"
    )
    # 발행 날짜
    issued_date = models.CharField(
        max_length=20,
        blank=True,
        null=True,
        verbose_name="발행 날짜"
    )
    # 알람 여부
    use_alarm = models.BooleanField(default=True)

    class Meta:
        db_table = "regihistory"
        verbose_name = "등록 이력"
        verbose_name_plural = "등록 이력 목록"

    def __str__(self):
        return f"regihistory #{self.id} (user={self.user})"



#복용 스케줄
class Plan(models.Model):
    #PK id자동생성
    # user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    regihistory = models.ForeignKey(
        RegiHistory,
        on_delete=models.CASCADE
    )

    med_name = models.CharField(null=True, max_length=120)
    taken_at = models.DateTimeField(null=True, blank=True)
    ex_taken_at = models.DateTimeField(null=True, blank=True)
    meal_time = models.CharField(
        max_length=20,
        choices=[
            ("before", "Before Meal"),
            ("after", "After Meal"),
            ("with", "With Meal"),
        ]
    )
    note = models.TextField(blank=True, null=True)
    taken = models.DateTimeField(null=True, blank=True)
    use_alarm = models.BooleanField(default=True)
    #관리용
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "plan"
        ordering = ["-created_at"]

    def __str__(self):
        return f" {self.med_name} ({self.taken_at})"