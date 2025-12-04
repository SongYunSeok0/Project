import logging
from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from notifications.services import send_fcm_to_token
from .models import Plan

logger = logging.getLogger("celery")   # Celery 전용 logger 사용


@shared_task
def send_med_alarms_task():
    """
    1분마다 실행되어, KST 기준 현재 복용해야 할 복약 스케줄에 대해
    FCM 알림을 발송하는 Task
    """

    now_kst = timezone.localtime()

    # '분' 단위로 맞추기
    start = now_kst.replace(second=0, microsecond=0)
    end = start + timedelta(minutes=1)

    logger.info(f"[MED] 복약 알림 체크 시작 → {start.strftime('%Y-%m-%d %H:%M')} (KST)")

    # ORM은 UTC 저장 값을 자동으로 KST로 변환하므로 비교는 정상적으로 처리됨
    plans = (
        Plan.objects.filter(
            use_alarm=True,
            taken_at__gte=start,
            taken_at__lt=end,
        )
        .select_related("regihistory__user")
    )

    count = 0

    for plan in plans:
        user = plan.regihistory.user
        token = getattr(user, "fcm_token", None)

        if not token:
            logger.warning(f"[MED] FCM 토큰 없음 → user_id={user.id}, username={user.username}")
            continue

        # 알림 메시지에 넣을 시간
        taken_time_kst = timezone.localtime(plan.taken_at)
        time_str = taken_time_kst.strftime("%H:%M")

        try:
            send_fcm_to_token(
                token=token,
                title="💊 약 드실 시간이에요!",
                body=f"{user.username}님, [{plan.med_name}] 복용 시간입니다. ({time_str})",
                data={
                    "type": "med_alarm",
                    "plan_id": str(plan.id),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK",
                },
            )

            count += 1
            logger.info(
                f"[MED] 복약 알림 성공 → user_id={user.id}, plan_id={plan.id}, time={time_str}"
            )

        except Exception as e:
            logger.error(
                f"[MED] 복약 알림 실패 → user_id={user.id}, plan_id={plan.id}, error={e}"
            )

    logger.info(f"[MED] 총 {count}건의 복약 알림 발송 완료")

    return f"{count}건의 복약 알림 발송 완료"
