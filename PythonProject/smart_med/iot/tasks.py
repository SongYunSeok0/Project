from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from medications.models import Plan   # ← 복용 스케줄(예정시각) 모델
from .utils import push_is_time
from firebase_admin import messaging

@shared_task
def check_schedule_and_push_is_time():
    now = timezone.now()
    start = now - timedelta(minutes=30)
    end = now + timedelta(minutes=30)

    due_plans = Plan.objects.filter(
        taken_at__gte=start,
        taken_at__lte=end,
        use_alarm=True,
    ).select_related("regihistory__user")

    for plan in due_plans:
        user = plan.regihistory.user
        device = user.iot_devices.first()  # IoT 디바이스 1개라고 가정
        if device:
            # 너가 만든 IoT 함수 (이미 존재한다고 가정)
            try:
                from .utils import push_is_time
                push_is_time(device, True)
            except Exception as e:
                print("IoT push error:", e)

    return f"{due_plans.count()}개의 IoT 장치에 시간 신호 발송"


@shared_task
def check_medication_schedule():
    now = timezone.localtime()

    current_hour = now.hour
    current_min = now.minute

    # taken_at이 null이면 알림 제외
    plans = Plan.objects.filter(
        use_alarm=True,
        taken_at__hour=current_hour,
        taken_at__minute=current_min,
    ).select_related("regihistory__user")

    count = 0

    for p in plans:
        user = p.regihistory.user
        if not user.fcm_token:
            continue

        message = messaging.Message(
            notification=messaging.Notification(
                title="복약 알림",
                body=f"{user.username}님, {p.med_name} 복약 시간이 되었습니다.",
            ),
            token=user.fcm_token,
        )

        try:
            messaging.send(message)
            count += 1
        except Exception as e:
            print("FCM Error:", e)

    return f"{count}건의 복약 알림 발송"