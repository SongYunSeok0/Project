from __future__ import absolute_import, unicode_literals
import os
from celery import Celery
from celery.schedules import crontab

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'smart_med.settings')

app = Celery('smart_med')
app.config_from_object('django.conf:settings', namespace='CELERY')
app.autodiscover_tasks()

app.conf.beat_schedule = {
    # 약 복용 알림
    "send-med-alarms-every-minute": {
        "task": "medications.tasks.send_med_alarms_task",
        "schedule": crontab(minute="*"),  # 매 분
    },

    # IoT 복약 시간 체크
    "check-medication-every-minute": {
        "task": "iot.tasks.check_medication_schedule",
        "schedule": crontab(minute="*"),
    },

    # IoT 기기에게 "지금 약 먹을 시간인지" 신호 주기
    "check-medication-time-window": {
        "task": "iot.tasks.check_schedule_and_push_is_time",
        "schedule": 30,  # 30초
    },
}
