from django.contrib import admin
from .models import Medication, MedicationSchedule, MedicationHistory


class MedicationScheduleInline(admin.TabularInline):
    model = MedicationSchedule
    extra = 0
    fields = ("time", "days_of_week", "is_active", "created_at", "updated_at")
    readonly_fields = ("created_at", "updated_at")
    raw_id_fields = ("user",)


class MedicationHistoryInline(admin.TabularInline):
    model = MedicationHistory
    extra = 0
    fields = ("status", "due_at", "taken_at", "source", "note", "recorded_at")
    readonly_fields = ("status", "due_at", "taken_at", "source", "note", "recorded_at")
    can_delete = False
    show_change_link = True
    raw_id_fields = ("user", "schedule")


@admin.register(Medication)
class MedicationAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "name", "dose", "start_date", "end_date", "updated_at", "schedules_count")
    list_filter = ("start_date", "end_date")
    search_fields = ("name", "user__email", "user__username")
    ordering = ("-updated_at",)
    readonly_fields = ("created_at", "updated_at")
    raw_id_fields = ("user",)
    inlines = [MedicationScheduleInline, MedicationHistoryInline]
    list_per_page = 50

    def schedules_count(self, obj):
        return obj.medicationschedule_set.count()
    schedules_count.short_description = "스케줄 수"


@admin.register(MedicationSchedule)
class MedicationScheduleAdmin(admin.ModelAdmin):
    list_display = ("id", "medication", "user", "time", "is_active", "updated_at")
    list_filter = ("is_active",)
    search_fields = ("medication__name", "user__email", "user__username")
    readonly_fields = ("created_at", "updated_at")
    raw_id_fields = ("user", "medication")
    ordering = ("medication", "time")
    list_per_page = 50


@admin.register(MedicationHistory)
class MedicationHistoryAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "medication", "schedule", "status", "due_at", "taken_at", "source", "recorded_at")
    list_filter = ("status", "source")
    search_fields = ("medication__name", "user__email", "user__username", "note")
    date_hierarchy = "due_at"
    readonly_fields = ("recorded_at",)
    raw_id_fields = ("user", "medication", "schedule")
    ordering = ("-due_at",)
    list_per_page = 50
