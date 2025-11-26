from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from medications.models import Plan   # ← 복용 스케줄(예정시각) 모델
from .utils import push_is_time

@shared_task
def check_schedule_and_push_is_time():
    now = timezone.now()
    start = now - timedelta(minutes=30)
    end = now + timedelta(minutes=30)

    due_plans = Plan.objects.filter(
        taken_at__gte=start,
        taken_at__lte=end
    ).select_related("regihistory__user")

    for plan in due_plans:
        user = plan.regihistory.user
        device = user.iot_devices.first()
        if device:
            push_is_time(device, True)

