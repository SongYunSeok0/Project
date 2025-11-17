from django.contrib import admin
from .models import Prescription, Plan


# --------------------------
#  복용 스케줄 인라인 (처방전 상세 내에서)
# --------------------------
class PlanInline(admin.TabularInline):
    model = Plan
    extra = 0
    fields = (
        "id",             # plan_id 대신 Django 기본 PK
        "med_name",       # medicine_name → med_name
        "taken_at",
        "meal_time",
        "note",
        "taken",
    )
    readonly_fields = ("id",)
    show_change_link = True


# --------------------------
#  처방전 관리자
# --------------------------
@admin.register(Prescription)
class PrescriptionAdmin(admin.ModelAdmin):
    list_display = (
        "prescription_id",
        "user",
        "prescription_type",
        "disease_name",
        "issued_date",
    )
    list_filter = ("prescription_type", "issued_date")
    search_fields = ("user", "disease_name", "prescription_type")
    ordering = ("-issued_date",)
    inlines = [PlanInline]
    list_per_page = 50


# --------------------------
#  복용 스케줄 관리자
# --------------------------
@admin.register(Plan)
class PlanAdmin(admin.ModelAdmin):
    list_display = (
        "id",             # plan_id → id
        "user",           # user_id → user
        "prescription",   # prescription_id → prescription
        "med_name",       # medicine_name → med_name
        "taken_at",
        "meal_time",
        "taken",
    )
    list_filter = ("meal_time",)
    search_fields = ("med_name", "user__email")
    list_per_page = 50