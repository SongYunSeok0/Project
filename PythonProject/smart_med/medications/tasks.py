from celery import shared_task
from django.utils import timezone
from datetime import timedelta
from .models import Plan
from notifications.services import send_fcm_to_token


@shared_task
def send_med_alarms_task():
    """
    [실제 운영용] Celery Beat가 1분마다 호출하는 함수입니다.
    현재 시간(분)과 일치하는 약을 찾아 '화면을 덮는 알람(ALARM)'을 보냅니다.
    """
    # 1. 현재 시간 설정 (UTC 기준)
    now_utc = timezone.now()

    # 2. 검색 범위 설정: '현재 분' ~ '1분 뒤' (초 단위 절삭)
    start_time = now_utc.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    # 로그용 한국 시간 변환
    now_kst = timezone.localtime(now_utc)
    print(f"[Celery] 복약 알림 체크 시작: {now_kst.strftime('%Y-%m-%d %H:%M')} (KST)")

    # 3. 데이터 조회 (알람 켜진 것만)
    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

    count = 0

    # 4. 순회 및 전송
    for plan in targets:
        try:
            # 데이터 유효성 검사
            if not plan.regihistory or not plan.regihistory.user:
                continue

            user = plan.regihistory.user
            token = getattr(user, 'fcm_token', None)

            # 한국 시간 문자열 (예: 14:30)
            plan_time_kst = timezone.localtime(plan.taken_at)
            plan_time_str = plan_time_kst.strftime('%H:%M')

            if token:
                # ⭐ 핵심: type="ALARM"으로 보내서 전체 화면 알림 트리거
                send_fcm_to_token(
                    token=token,
                    title="💊 약 드실 시간이에요!",
                    body=f"{user.username}님, [{plan.med_name}] 복용 시간입니다. ({plan_time_str})",
                    data={
                        "type": "ALARM",  # 앱에서 AlarmActivity를 띄우는 신호
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

    # Celery 작업 결과로 남길 문자열 리턴 (HTML 아님)
    return f"총 {count}건 전송 완료"