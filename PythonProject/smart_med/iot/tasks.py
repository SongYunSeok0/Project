import logging
from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from firebase_admin import messaging

from medications.models import Plan

logger = logging.getLogger("celery")   # ⭐ Celery용 로거 사용


# ============================================================
# 1. IoT 기기에게 "지금 약 먹을 시간인지" 신호 보내기
# ============================================================
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

    count = 0

    for plan in due_plans:
        user = plan.regihistory.user
        device = user.iot_devices.first()

        if not device:
            logger.warning(f"[IoT] 사용자 {user.id}의 등록된 기기 없음")
            continue

        try:
            from .utils import push_is_time
            push_is_time(device, True)
            count += 1
            logger.info(f"[IoT] is_time 신호 전송 성공 → device={device.device_uuid}")

        except Exception as e:
            logger.error(f"[IoT] is_time 신호 전송 실패 → device={device.device_uuid}, error={e}")

    logger.info(f"[IoT] 총 {count}개 기기에 is_time 신호 발송 완료")

    return f"{count}개의 IoT 장치에 시간 신호 발송"


# ============================================================
# 2. FCM 복약 알림
# ============================================================
@shared_task
def check_medication_schedule():
    now = timezone.localtime()
    hour = now.hour
    minute = now.minute

    plans = Plan.objects.filter(
        use_alarm=True,
        taken_at__hour=hour,
        taken_at__minute=minute,
    ).select_related("regihistory__user")

    count = 0

    for plan in plans:
        user = plan.regihistory.user

        if not user.fcm_token:
            logger.warning(f"[FCM] FCM 토큰 없음 → user_id={user.id}")
            continue

        message = messaging.Message(
            notification=messaging.Notification(
                title="복약 알림",
                body=f"{user.username}님, {plan.med_name} 복약 시간이 되었습니다.",
            ),
            token=user.fcm_token,
        )

        try:
            messaging.send(message)
            count += 1
            logger.info(f"[FCM] 복약 알림 전송 성공 → user_id={user.id}, plan_id={plan.id}")

        except Exception as e:
            logger.error(f"[FCM] 복약 알림 전송 실패 → user_id={user.id}, plan_id={plan.id}, error={e}")

    logger.info(f"[FCM] 총 {count}건의 복약 알림 발송 완료")

    return f"{count}건의 복약 알림 발송"
