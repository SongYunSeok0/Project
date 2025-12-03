from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from .models import Plan
from notifications.services import send_fcm_to_token


@shared_task
def send_med_alarms_task():
    """
    1분마다 실행되어, 정확히 현재 시간에 복용해야 할 약(Plan)을 찾아 알림을 전송합니다.
    """
    # 1. 현재 시간 설정 (UTC 기준)
    # 초(second) 단위는 버려서 '분' 단위로 정확히 매칭합니다.
    now = timezone.now()
    start_time = now.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    # 로그용 한국 시간 변환 (디버깅 편의성)
    now_kst = timezone.localtime(now)
    print(f"[Celery] 복약 알림 체크 시작: {now_kst.strftime('%Y-%m-%d %H:%M')} (KST)")

    # 2. DB 조회 조건
    # - use_alarm=True (알람 켜진 것만)
    # - taken_at이 현재 '분' 범위 내에 있는 것
    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')  # N+1 문제 방지

    count = 0

    # 3. 대상 순회 및 알림 전송
    for plan in targets:
        try:
            # 관계 데이터 유효성 검사
            if not plan.regihistory or not plan.regihistory.user:
                continue

            user = plan.regihistory.user
            token = getattr(user, 'fcm_token', None)

            if token:
                # 메시지 본문에 넣을 시간 (예: 12:30)
                plan_time_kst = timezone.localtime(plan.taken_at)
                time_str = plan_time_kst.strftime('%H:%M')

                # FCM 전송 (notifications/services.py의 함수 사용)
                send_fcm_to_token(
                    token=token,
                    title="💊 약 드실 시간이에요!",
                    body=f"{user.username}님, [{plan.med_name}] 복용 시간입니다. ({time_str})",
                    data={
                        "type": "med_alarm",
                        "plan_id": str(plan.id),
                        "click_action": "FLUTTER_NOTIFICATION_CLICK"
                    }
                )
                print(f" -> [전송 성공] {user.username} / {plan.med_name}")
                count += 1
            else:
                print(f" -> [전송 실패] {user.username}: FCM 토큰 없음")

        except Exception as e:
            print(f" -> [에러 발생] Plan ID {plan.id}: {e}")

    return f"총 {count}건의 알림 전송 완료"