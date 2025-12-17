from __future__ import absolute_import, unicode_literals
import os
from celery import Celery
from celery.schedules import crontab

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'smart_med.settings')

app = Celery('smart_med')
app.config_from_object('django.conf:settings', namespace='CELERY')
app.autodiscover_tasks()

# ======================================================
#                CELERY BEAT SCHEDULE
# ======================================================

app.conf.beat_schedule = {

    # --------------------------------------------------
    # Medication / 복약 알림 관련 작업
    # --------------------------------------------------

    "medication_send_alarms_every_minute": {
        "task": "medications.tasks.send_med_alarms_task",
        "schedule": crontab(minute="*"),  # 매 분 실행
    },

    "medication_check_missed_every_minute": {
        "task": "medications.tasks.check_missed_medication",
        "schedule": crontab(minute="*"), # 매 1분마다 체크 (운영 시 */10 등으로 변경 가능)
    },

    # 2. [통합] 재알림 (10분, 20분 체크)
    "medication_user_reminders_every_minute": {
        "task": "medications.tasks.send_user_reminders_task", # 👈 새로 만든 함수
        "schedule": crontab(minute="*"),
    },

    # "medication_check_time_window": {
    #     "task": "iot.tasks.check_medication_schedule",
    #     "schedule": crontab(minute="*"),  # 매 분 실행
    # },

    # --------------------------------------------------
    # IoT Device / IoT 장치 폴링 신호 전송
    # --------------------------------------------------

    "iot_send_is_time_signal_every_30s": {
        "task": "iot.tasks.check_schedule_and_push_is_time",
        "schedule": 30,  # 30초 간격 실행
    },
}
