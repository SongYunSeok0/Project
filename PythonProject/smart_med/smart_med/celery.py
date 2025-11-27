from __future__ import absolute_import, unicode_literals
import os
from celery import Celery
from celery.schedules import crontab   # ★ beat 일정 사용하려면 필요

# Django 설정 로드
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'smart_med.settings')

# Celery 앱 생성
app = Celery('smart_med')

# Django settings의 CELERY_ 네임스페이스 읽기
app.config_from_object('django.conf:settings', namespace='CELERY')

# 앱 내부의 tasks.py 자동 로드
app.autodiscover_tasks()

app.conf.beat_schedule = {
    # 사용자 스마트폰 복약 알림 (정확한 시간)
    "check-medication-every-minute": {
        "task": "iot.tasks.check_medication_schedule",
        "schedule": crontab(minute="*"),  # 매 분
    },

    # IoT 디바이스에 "복약할 시간대인지" 알려주는 기능 (30초마다)
    "check-medication-time-window": {
        "task": "iot.tasks.check_schedule_and_push_is_time",
        "schedule": 30,  # 30초마다 실행
    },
}