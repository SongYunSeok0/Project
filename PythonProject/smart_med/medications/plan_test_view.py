from django.shortcuts import HttpResponse
from django.utils import timezone
from datetime import timedelta
from .models import Plan
from notifications.services import send_fcm_to_token


def test_med_alarm_view(request):
    """
    [실전 테스트용]
    현재 시간(분)과 정확히 일치하는 복약 일정(Plan)을 찾아 알림을 보냅니다.
    (Celery Task 로직과 동일한 조건: '분' 단위 매칭 & use_alarm=True)
    """
    # 1. 현재 시간 설정 (UTC 기준)
    now_utc = timezone.now()

    # 2. 검색 범위 설정: '현재 분' ~ '1분 뒤' (초 단위 절삭)
    start_time = now_utc.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    # 3. 로그용 한국 시간 변환
    now_kst = timezone.localtime(now_utc)

    print(f"\n=== [TEST View] 실전 알림 체크 시작 ===")
    print(f"1. 현재 서버 시간 (KST): {now_kst.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"2. DB 검색 범위 (UTC): {start_time.strftime('%H:%M')} ~ {end_time.strftime('%H:%M')}")

    # 4. 데이터 조회
    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

    total_count = targets.count()
    print(f"3. 검색된 알림 대상: {total_count}개")

    count = 0
    result_log = []

    result_log.append(f"<b>현재 서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>")
    result_log.append(f"<b>검색 기준:</b> 정확히 현재 '분'에 해당하는 약만 조회<hr>")

    if total_count == 0:
        msg = "⚠️ 현재 시간에 복용해야 할 약이 없습니다. (혹은 알람이 꺼져있음)"
        print(msg)
        result_log.append(msg)
    else:
        for plan in targets:
            try:
                plan_time_kst = timezone.localtime(plan.taken_at)
                plan_time_str = plan_time_kst.strftime('%H:%M')

                if not plan.regihistory or not plan.regihistory.user:
                    continue

                user = plan.regihistory.user
                token = getattr(user, 'fcm_token', None)

                if token:
                    # ⭐ FCM 발송 - type을 "ALARM"으로 변경!
                    send_fcm_to_token(
                        token=token,
                        title="💊 약 드실 시간이에요!",
                        body=f"{user.username}님, [{plan.med_name}] 복용 시간입니다. ({plan_time_str})",
                        data={
                            "type": "ALARM",  # 👈 med_alarm → ALARM 변경!
                            "plan_id": str(plan.id),
                            "click_action": "FLUTTER_NOTIFICATION_CLICK"
                        }
                    )
                    log = f"✅ <b>전송 성공:</b> {user.username} / {plan.med_name} (목표시간: {plan_time_str})"
                    print(log)
                    result_log.append(log)
                    count += 1
                else:
                    msg = f"❌ [실패] {user.username}: FCM 토큰 없음"
                    print(msg)
                    result_log.append(msg)

            except Exception as e:
                err = f"⚠️ 에러 (Plan ID: {plan.id}): {e}"
                print(err)
                result_log.append(err)

    print(f"=== [TEST View] 테스트 종료: 총 {count}건 전송 ===\n")

    return HttpResponse(
        f"<h1>🔔 실전 알림 테스트 결과</h1>"
        f"<p><b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}</p>"
        f"<p><b>실제 전송 성공:</b> {count}건 / {total_count}건</p>"
        f"<hr>"
        f"<br>".join(result_log)
    )