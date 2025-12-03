from django.shortcuts import HttpResponse
from django.utils import timezone
from datetime import timedelta
from .models import Plan
from notifications.services import send_fcm_to_token


def test_med_alarm_view(request):
    """
    [테스트용] 한국 시간(KST) 기준으로 로그를 출력하며 알림을 테스트합니다.
    """
    # 1. 현재 시간 가져오기 (UTC)
    now_utc = timezone.now()

    # 2. 한국 시간(KST)으로 변환 (settings.TIME_ZONE이 'Asia/Seoul'이어야 함)
    now_kst = timezone.localtime(now_utc)

    # 3. 검색 범위 설정 (테스트용 앞뒤 12시간)
    # DB 조회는 UTC 기준인 'now_utc'를 사용하는 것이 안전합니다. (Django가 알아서 처리)
    start_time = now_utc - timedelta(hours=12)
    end_time = now_utc + timedelta(hours=12)

    print(f"\n=== [TEST View] 알림 테스트 시작 ===")
    print(f"1. 서버 현재 시간 (UTC): {now_utc.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"2. 서버 현재 시간 (KST): {now_kst.strftime('%Y-%m-%d %H:%M:%S')}  <-- 한국 시간")

    # 4. 데이터 조회
    # Django ORM은 USE_TZ=True일 때, UTC 시간을 넣으면 알아서 DB와 비교해줍니다.
    targets = Plan.objects.filter(
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

    total_count = targets.count()
    print(f"3. 검색된 Plan 개수: {total_count}개")

    count = 0
    result_log = []

    # 화면 출력용 문구
    result_log.append(f"<b>현재 서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>")
    result_log.append(f"<b>검색된 데이터:</b> {total_count}건<hr>")

    for plan in targets:
        try:
            # DB에 저장된 시간을 한국 시간으로 변환해서 로그에 출력
            plan_time_kst = timezone.localtime(plan.taken_at)
            plan_time_str = plan_time_kst.strftime('%Y-%m-%d %H:%M:%S')

            user_name = "알수없음"
            if plan.regihistory and plan.regihistory.user:
                user_name = plan.regihistory.user.username

            # 알람 설정 체크
            if not plan.use_alarm:
                msg = f"[스킵] {user_name} / {plan.med_name} (복용시간: {plan_time_str}) - 알람 OFF"
                print(msg)
                result_log.append(msg)
                continue

            if not plan.regihistory or not plan.regihistory.user:
                continue

            user = plan.regihistory.user
            token = getattr(user, 'fcm_token', None)

            if token:
                # 알림 발송
                send_fcm_to_token(
                    token=token,
                    title="[테스트] 💊 약 드실 시간이에요!",
                    # 메시지에도 한국 시간을 넣어줍니다.
                    body=f"{user.username}님, [{plan.med_name}] 복용 시간입니다. ({plan_time_kst.strftime('%H:%M')})",
                    data={
                        "type": "med_alarm",
                        "plan_id": str(plan.id)
                    }
                )
                log = f"✅ <b>전송 성공:</b> {user.username} / {plan.med_name} / <b>복용시간(KST): {plan_time_str}</b>"
                print(log)
                result_log.append(log)
                count += 1
            else:
                msg = f"❌ [실패] {user_name}: 토큰 없음"
                print(msg)
                result_log.append(msg)

        except Exception as e:
            err = f"⚠️ 에러 (Plan ID: {plan.id}): {e}"
            print(err)
            result_log.append(err)

    print("=== [TEST View] 테스트 종료 ===\n")

    return HttpResponse(
        f"<h1>알림 테스트 결과 (KST 기준)</h1>"
        f"<p>현재 서버 시간: {now_kst.strftime('%Y-%m-%d %H:%M:%S')}</p>"
        f"<p>성공 건수: {count}</p>"
        f"<hr>"
        f"<br>".join(result_log)
    )