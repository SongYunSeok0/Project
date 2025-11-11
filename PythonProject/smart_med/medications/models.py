from django.db import models
from django.conf import settings



#약 기본 정보
class Medication(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    name = models.CharField(max_length=200)
    dose = models.CharField(max_length=60, blank=True, null=True)
    start_date = models.DateField()
    end_date = models.DateField(blank=True, null=True)
    source = models.CharField(max_length=10, default="manual")  # manual | ocr
    note = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.name} ({self.user.username})"



#복용 스케줄
class MedicationSchedule(models.Model):
    medication = models.ForeignKey(Medication, on_delete=models.CASCADE)
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    time = models.TimeField()
    days_of_week = models.JSONField(default=list, blank=True)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ("medication", "time", "user")
        ordering = ["time"]

    def __str__(self):
        return f"{self.medication.name} - {self.time}"



#복용 이력
class MedicationHistory(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    medication = models.ForeignKey(Medication, on_delete=models.CASCADE)
    schedule = models.ForeignKey(MedicationSchedule, on_delete=models.SET_NULL, null=True, blank=True)
    status = models.CharField(
        max_length=10,
        choices=[
            ("taken", "복용 완료"),
            ("missed", "미복용"),
            ("delayed", "지연 복용"),
            ("skipped", "복용 건너뜀"),
        ],
    )
    due_at = models.DateTimeField()
    taken_at = models.DateTimeField(blank=True, null=True)
    source = models.CharField(max_length=10, default="manual")  # manual | iot | auto
    note = models.TextField(blank=True)
    recorded_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-due_at"]

    def __str__(self):
        return f"{self.medication.name} - {self.status}"



#약 정보 (공공데이터)
class DrugInfo(models.Model):
    item_name = models.CharField(max_length=200, db_index=True)  # 제품명
    efcy_qesitm = models.TextField(blank=True, null=True)        # 효능/효과
    use_method_qesitm = models.TextField(blank=True, null=True)  # 용법/용량
    atpn_warn_qesitm = models.TextField(blank=True, null=True)   # 사용상 주의사항(경고)
    atpn_qesitm = models.TextField(blank=True, null=True)        # 주의사항
    intrc_qesitm = models.TextField(blank=True, null=True)       # 상호작용
    se_qesitm = models.TextField(blank=True, null=True)          # 부작용
    imported_at = models.DateTimeField(auto_now_add=True)        # 데이터 수집 시각

    class Meta:
        db_table = "medications_druginfo"
        ordering = ["item_name"]

    def __str__(self):
        return self.item_name
