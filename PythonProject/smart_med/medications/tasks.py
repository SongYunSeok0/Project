import logging
from celery import shared_task
from django.contrib.auth import get_user_model
from django.utils import timezone
from datetime import timedelta
from notifications.services import send_fcm_to_token, initialize_firebase
from .models import Plan
from firebase_admin import messaging
from django.core.cache import cache

logger = logging.getLogger("celery")   # Celery 전용 logger 사용

User = get_user_model()

@shared_task
def send_med_alarms_task():
    """
    1분마다 실행되어, 정확히 현재 시간에 복용해야 할 약(Plan)을 찾아 알림을 전송합니다.
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

    return f"총 {count}건 전송 완료"


# ==========================================
# 2.보호자 미복용 알림 (30분 지연 체크)
# ==========================================
@shared_task
def check_missed_medication():
    """
    미복용 시 보호자에게 알림 전송.
    보호자 알림은 '전체 화면 알람'이 아닌 '일반 알림(Notification)' 형식이므로
    initialize_firebase()만 호출하고 메시지는 직접 구성합니다.
    """
    # services.py의 초기화 함수 호출 (연결 보장)
    initialize_firebase()

    now = timezone.now()
    end_time = now - timedelta(minutes=30)
    start_time = now - timedelta(days=1)

    missed_plans = Plan.objects.filter(
        taken_at__range=(start_time, end_time),
        taken__isnull=True
    ).select_related('regihistory__user')

    count = 0

    for plan in missed_plans:
        cache_key = f"missed_noti_sent:{plan.id}"
        if cache.get(cache_key):
            continue

        try:
            patient = plan.regihistory.user
            guardian_email = patient.prot_email

            if guardian_email:
                guardian = User.objects.filter(email=guardian_email).first()
                if guardian and guardian.fcm_token:
                    # 보호자용: 표준 Notification 메시지 구성
                    # (send_fcm_to_token은 data 메시지 전용이라 직접 구성함)

                    message = messaging.Message(
                        notification=messaging.Notification(
                            title="🚨 미복용 알림",
                            body=f"{patient.username}님이 [{plan.med_name}] 약을 아직 복용하지 않았습니다."
                        ),
                        token=guardian.fcm_token,
                    )
                    messaging.send(message)
                    print(f" -> [보호자 알림] {patient.username} -> {guardian_email}")
                    count += 1

            # 24시간 동안 재발송 방지
            cache.set(cache_key, "True", timeout=86400)

        except Exception as e:
            print(f" -> [보호자 알림 실패] Plan ID {plan.id}: {e}")

    return f"미복용 체크 완료: {count}건 발송"