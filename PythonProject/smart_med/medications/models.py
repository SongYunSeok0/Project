from django.db import models
from django.conf import settings



#처방전
class Prescription(models.Model):
    # 사용자 ID (ForeignKey로 User 테이블과 연결 가능)
    user = models.IntegerField(null=False, verbose_name="사용자 ID")

    # 처방전 고유 ID
    prescription_id = models.AutoField(primary_key=True, verbose_name="처방전 ID")

    # 처방전 유형 (영양제 / 병원약 등)
    prescription_type = models.CharField(
        max_length=50,
        verbose_name="처방전 유형 (영양제/병원약)"
    )

    # 병명
    disease_name = models.CharField(
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
        db_table = "prescription"
        verbose_name = "사용자 처방전"
        verbose_name_plural = "사용자 처방전 목록"

    def __str__(self):
        return f"{self.user} - {self.prescription_type} ({self.issued_date})"



#복용 스케줄
class Plan(models.Model):
    #PK id자동생성
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    prescription= models.ForeignKey(Prescription, on_delete=models.CASCADE)
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
    note = models.TextField(blank=True)
    taken = models.TimeField(null=True, blank=True)
    #관리용
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "plan"
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.user} , {self.med_name} ({self.taken_at})"




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
