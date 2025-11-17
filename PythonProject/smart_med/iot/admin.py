# iot/admin.py
from django.contrib import admin
from .models import Device, SensorData


@admin.register(Device)
class DeviceAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "is_active", "last_connected_at")
    list_filter = ("is_active",)
    search_fields = ("user__email", "user__username")
    ordering = ("-last_connected_at",)
    readonly_fields = ("last_connected_at",)
    verbose_name = "IoT Device"
    verbose_name_plural = "IoT Devices"


@admin.register(SensorData)
class SensorDataAdmin(admin.ModelAdmin):
    list_display = ("id", "device", "user", "is_time", "is_opened", "collected_at", "created_at")
    list_filter = ("is_opened", "is_time", "collected_at")
    search_fields = ("device__id", "user__email")
    ordering = ("-collected_at",)
    readonly_fields = ("created_at",)

    class Meta:
        ordering = ["-collected_at"]
        verbose_name = "Sensor Data"
        verbose_name_plural = "Sensor Data"
