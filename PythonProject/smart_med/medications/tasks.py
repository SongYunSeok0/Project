from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from django.contrib.auth import get_user_model
from django.core.cache import cache
from firebase_admin import messaging
from .models import Plan
from notifications.services import send_fcm_to_token, initialize_firebase

User = get_user_model()


# ==========================================
# 1.환자 복용 알림 (정시 발송)
# ==========================================
@shared_task
def send_med_alarms_task():
    """
    [실제 운영용] Celery Beat가 1분마다 호출하는 함수.
    notifications.services의 send_fcm_to_token을 사용하여 알람을 전송합니다.
    """
    # 1. 현재 시간 설정
    now_utc = timezone.now()
    start_time = now_utc.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    # 로그용 한국 시간
    now_kst = timezone.localtime(now_utc)
    print(f"[Celery] 복약 알림 체크 시작: {now_kst.strftime('%Y-%m-%d %H:%M')} (KST)")

    # 2. 데이터 조회
    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

    count = 0

    # 3. 순회 및 전송
    for plan in targets:
        try:
            if not plan.regihistory or not plan.regihistory.user:
                continue

            user = plan.regihistory.user
            token = getattr(user, 'fcm_token', None)

            plan_time_kst = timezone.localtime(plan.taken_at)
            plan_time_str = plan_time_kst.strftime('%H:%M')

            if token:
                # notifications/services.py의 함수 사용
                # 내부에서 initialize_firebase()가 호출되므로 별도 초기화 불필요
                res = send_fcm_to_token(
                    token=token,
                    title="💊 약 드실 시간이에요!",
                    body=f"{user.username}님, [{plan.med_name}] 복용 시간입니다. ({plan_time_str})",
                    data={
                        "type": "ALARM",  # 앱에서 AlarmActivity 트리거
                        "plan_id": str(plan.id),
                        "click_action": "FLUTTER_NOTIFICATION_CLICK"
                    }
                )
                print(f" -> [전송 성공] {user.username} / {plan.med_name}: {res}")
                count += 1
            else:
                print(f" -> [전송 실패] {user.username}: FCM 토큰 없음")

        except Exception as e:
            print(f" -> [에러 발생] Plan ID {plan.id}: {e}")

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