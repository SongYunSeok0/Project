from celery import shared_task
from medications.models import MedicationHistory
from users.models import Protector
from firebase_admin import messaging

@shared_task
def check_missed_doses():
    # 최근 3회 연속 미복용 사용자 탐색
    from datetime import timedelta
    from django.utils import timezone

    now = timezone.now()
    recent = now - timedelta(days=3)
    missed_users = (
        MedicationHistory.objects
        .filter(status='missed', taken_time__gte=recent)
        .values('medication__user')
        .distinct()
    )

    for item in missed_users:
        user_id = item['medication__user']
        protectors = Protector.objects.filter(user_id=user_id)
        for protector in protectors:
            # 보호자에게 알림 전송
            send_notification.delay(protector.phone, "3회 이상 복용 누락 발생")
