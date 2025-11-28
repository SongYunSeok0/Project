from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from .models import Plan
from notifications.services import send_fcm_to_token  # 알림 발송 기능 가져오기

@shared_task
def send_med_alarms_task():
    """
    1분마다 실행되어, 복용 시간이 된 Plan을 찾아 FCM 알림을 전송합니다.
    """
    # 1. 현재 시간 기준, '분' 단위 범위 설정 (초 단위 무시)
    now = timezone.now()
    start_time = now.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    print(f"[Celery] 복약 알림 체크 중... ({start_time.strftime('%H:%M')})")

    # 2. 조건에 맞는 Plan 조회
    # - use_alarm이 True이고
    # - taken_at(복용시간)이 지금(현재 1분 구간)인 것
    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user') # DB 최적화 (User까지 한번에 로딩)

    count = 0
    for plan in targets:
        # Plan -> RegiHistory -> User 순서로 접근하여 토큰 확인
        try:
            # 관계가 끊겨있을 수도 있으므로 안전하게 접근
            if not plan.regihistory or not plan.regihistory.user:
                continue

            user = plan.regihistory.user
            token = getattr(user, 'fcm_token', None)

            if token:
                # 3. 알림 발송!
                send_fcm_to_token(
                    token=token,
                    title="💊 약 드실 시간이에요!",
                    body=f"{user.username}님, [{plan.med_name}] 복용할 시간입니다.",
                    data={
                        "type": "med_alarm",     # 안드로이드에서 구분할 태그
                        "plan_id": str(plan.id)  # 필요 시 알림 클릭하면 해당 약 정보로 이동
                    }
                )
                print(f" -> 알림 전송 완료: {user.username} / {plan.med_name}")
                count += 1
        except Exception as e:
            print(f" -> 알림 전송 실패 (Plan ID: {plan.id}): {e}")

    return f"총 {count}건의 알림 전송 완료"