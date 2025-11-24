from django.db import models
from django.conf import settings



# 등록 이력(기존 Prescription)
class RegiHistory(models.Model):
    # 사용자 ID (ForeignKey로 User 테이블과 연결 가능)

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        verbose_name="사용자 ID"
    )

    # 고유 ID 자동 생성
    # id = models.BigAutoField(
    #     primary_key=True,
    #     verbose_name="등록 이력 ID"
    # )

    # 등록 유형 (영양제 / 병원약 등)
    regi_type = models.CharField(
        max_length=50,
        verbose_name="등록 유형 (영양제/병원약)"
    )

    # 병명 (기존 disase_name)
    label = models.CharField(
        max_length=100,
        blank=True,
        null=True,
        verbose_name="병명"
    )

    # 발행 날짜
    issued_date = models.CharField(
        max_length=20,
        blank=True,
        null=True,
        verbose_name="발행 날짜"
    )

    class Meta:
        db_table = "regiHistory"
        verbose_name = "등록 이력"
        verbose_name_plural = "등록 이력 목록"

    def __str__(self):
        return f"RegiHistory #{self.id} (user={self.user})"



#복용 스케줄
class Plan(models.Model):
    #PK id자동생성
    # user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    regihistory = models.ForeignKey(
        RegiHistory,
        on_delete=models.CASCADE,
        null=True,  # ← 필수!!
        blank=True  # ← 필수!!
    )

    med_name = models.CharField(max_length=120)
    taken_at = models.DateTimeField(null=True, blank=True)
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
    #관리용
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "plan"
        ordering = ["-created_at"]

    def __str__(self):
        return f" {self.med_name} ({self.taken_at})"