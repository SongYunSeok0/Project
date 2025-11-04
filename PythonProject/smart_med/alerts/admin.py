from django.contrib import admin
from .models import Device,SensorData

admin.site.register(Device)

@admin.register(SensorData)
class SensorDataAdmin(admin.ModelAdmin):
    list_display = ('id', 'sensor_value', 'heart_rate', 'is_opened', 'timestamp')  # 표시할 컬럼
    list_filter = ('is_opened', 'timestamp')  # 필터 사이드바
    search_fields = ('sensor_value', 'heart_rate')  # 검색 가능 필드
    ordering = ('-timestamp',)  # 최신순 정렬

    class Meta:
        ordering = ['-timestamp']
        verbose_name = "Sensor Data"
        verbose_name_plural = "Sensor Data"