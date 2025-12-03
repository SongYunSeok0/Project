from django.shortcuts import HttpResponse
from django.utils import timezone
from datetime import timedelta
from .models import Plan
from notifications.services import send_fcm_to_token


def test_med_alarm_view(request):
    """
    [테스트용] 브라우저에서 접속하면 즉시 알림 로직을 실행하는 뷰
    """
    # --- 기존 tasks.py 로직 시작 ---
    now = timezone.now()
    start_time = now.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    print(f"[TEST View] 복약 알림 수동 체크 중... ({start_time.strftime('%H:%M')})")

    # 테스트를 위해 범위를 조금 넓혀서 확인하고 싶다면 아래 주석을 푸세요
    # end_time = start_time + timedelta(minutes=60) # 향후 1시간치 조회

    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

    count = 0
    result_log = []  # 화면에 뿌려줄 로그 저장용

    for plan in targets:
        try:
            if not plan.regihistory or not plan.regihistory.user:
                continue

            user = plan.regihistory.user
            token = getattr(user, 'fcm_token', None)

            if token:
                # 실제 FCM 발송
                send_fcm_to_token(
                    token=token,
                    title="[테스트] 💊 약 드실 시간이에요!",
                    body=f"{user.username}님, [{plan.med_name}] 복용할 시간입니다.",
                    data={
                        "type": "med_alarm",
                        "plan_id": str(plan.id)
                    }
                )
                log = f"성공: {user.username} / {plan.med_name}"
                print(log)
                result_log.append(log)
                count += 1
            else:
                result_log.append(f"실패(토큰없음): {user.username}")

        except Exception as e:
            err = f"에러 (Plan ID: {plan.id}): {e}"
            print(err)
            result_log.append(err)

    # --- 로직 끝 ---

    return HttpResponse(
        f"<h1>알림 테스트 완료</h1>"
        f"<p>현재시간: {now}</p>"
        f"<p>전송 건수: {count}</p>"
        f"<br>".join(result_log)
    )