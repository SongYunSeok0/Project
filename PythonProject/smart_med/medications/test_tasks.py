from django.shortcuts import HttpResponse
from django.utils import timezone
from datetime import timedelta
from django.contrib.auth import get_user_model
from django.core.cache import cache
from firebase_admin import messaging
from .models import Plan

# notifications 앱의 services.py에서 초기화 함수와 전송 함수 가져오기
from notifications.services import send_fcm_to_token, initialize_firebase

User = get_user_model()


# ====================================================
# 1. [View] 환자 정시 복용 알림 postman용 테스트
# ====================================================
def test_med_alarm_view(request):
    """
    [테스트용] tasks.py의 send_med_alarms_task 로직을 수동 실행합니다.
    현재 분(minute)에 복용해야 할 약을 찾아 환자에게 전체화면 알람(ALARM)을 보냅니다.
    """
    # send_fcm_to_token 내부에서 initialize_firebase()가 호출되므로
    # 여기서는 별도로 호출하지 않아도 안전하지만, 명시적으로 호출해도 무방합니다.
    # initialize_firebase()

    now_utc = timezone.now()
    now_kst = timezone.localtime(now_utc)

    # 1. 검색 범위: 현재 분 ~ 1분 뒤 (초 단위 절삭)
    start_time = now_utc.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    print(f"\n=== [TEST Regular] 환자 정시 알림 체크 시작 ({now_kst.strftime('%H:%M')}) ===")

    # 2. 데이터 조회 (알람 켜진 것만)
    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

    # 3. 결과 로그 초기화
    result_log = [
        f"<b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>",
        f"<b>검색 범위(UTC):</b> {start_time.strftime('%H:%M')} ~ {end_time.strftime('%H:%M')}<hr>"
    ]

    count = 0
    total_count = targets.count()

    if total_count == 0:
        msg = "⚠️ 현재 시간에 복용해야 할 약이 없습니다."
        print(msg)
        result_log.append(msg)
    else:
        # 4. 개별 처리 로직 호출
        for plan in targets:
            success, log_msg = _process_regular_alarm(plan)
            result_log.append(log_msg)
            if success:
                count += 1

    print(f"=== [TEST Regular] 종료: {count}건 전송 ===\n")
    return _build_response("🔔 환자 정시 알림 테스트", now_kst, count, total_count, result_log)


# ====================================================
# 2. [View] 보호자 미복용 알림 테스트 (30분 지연)
# ====================================================
def test_missed_alarm_view(request):
    """
    [테스트용] tasks.py의 check_missed_medication 로직을 수동 실행합니다.
    30분이 지났는데 미복용(taken is NULL)인 건에 대해 보호자에게 알림을 보냅니다.
    """
    # [수정됨] services.py의 초기화 함수 호출 (Firebase 연결 보장)
    # 아래 로직에서 messaging.send()를 직접 쓰기 때문에 반드시 필요합니다.
    initialize_firebase()

    now = timezone.now()
    now_kst = timezone.localtime(now)

    # 1. 검색 범위: 30분 전 ~ 24시간 전
    # end_time = now - timedelta(minutes=30)
    end_time = now - timedelta(minutes=30)
    start_time = now - timedelta(days=1)

    print(f"\n=== [TEST Missed] 미복용(보호자) 체크 시작 ({now_kst.strftime('%H:%M')}) ===")

    # 2. 데이터 조회 (아직 안 먹은 약만)
    missed_plans = Plan.objects.filter(
        taken_at__range=(start_time, end_time),
        taken__isnull=True
    ).select_related('regihistory__user')

    # 3. 결과 로그 초기화
    result_log = [
        f"<b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>",
        f"<b>검색 범위:</b> 30분 전 ~ 24시간 전<hr>"
    ]

    count = 0
    total_count = missed_plans.count()

    # URL 파라미터 ?force=true 가 있으면 캐시 무시하고 강제 전송
    is_force = request.GET.get('force') == 'true'

    if total_count == 0:
        msg = "✅ 미복용 상태인 건이 없거나, 아직 30분이 지나지 않았습니다."
        print(msg)
        result_log.append(msg)
    else:
        # 4. 개별 처리 로직 호출
        for plan in missed_plans:
            success, log_msg = _process_missed_alarm(plan, is_force)
            result_log.append(log_msg)
            if success:
                count += 1

    print(f"=== [TEST Missed] 종료: {count}건 전송 ===\n")
    return _build_response("🚨 미복용 알림(보호자) 테스트", now_kst, count, total_count, result_log)


# ====================================================
# 3. [Helper] 내부 로직 함수들 (실제 기능 수행)
# ====================================================

def _process_regular_alarm(plan):
    """
    환자 정시 알림 1건을 처리하고 결과를 반환합니다. (type="ALARM")
    """
    try:
        if not plan.regihistory or not plan.regihistory.user:
            return False, f"⚠️ 데이터 오류 (Plan {plan.id})"

        user = plan.regihistory.user
        token = getattr(user, 'fcm_token', None)

        # 한국 시간 변환 (로그 및 메시지용)
        plan_time_str = timezone.localtime(plan.taken_at).strftime('%H:%M')

        if token:
            # ⭐ 핵심: type="ALARM"으로 보내서 전체 화면 알림 트리거
            # send_fcm_to_token 내부에서 initialize_firebase()를 수행하므로 안전함
            send_fcm_to_token(
                token=token,
                title="💊 약 드실 시간이에요!",
                # [수정] plan.med_name 대신 regihistory.label 사용
                body=f"{user.username}님, [{plan.regihistory.label}] 복용 시간입니다. ({plan_time_str})",
                data={
                    "type": "ALARM",  # 앱에서 AlarmActivity를 띄우는 신호
                    "plan_id": str(plan.id),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                }
            )
            log = f"✅ <b>[전송 성공]</b> {user.username} / {plan.med_name} ({plan_time_str})"
            print(log)
            return True, log
        else:
            log = f"❌ [실패] {user.username}: FCM 토큰 없음"
            print(log)
            return False, log

    except Exception as e:
        err = f"⚠️ 에러 (Plan {plan.id}): {e}"
        print(err)
        return False, err


def _process_missed_alarm(plan, is_force=False):
    """
    보호자 미복용 알림 1건을 처리하고 결과를 반환합니다.
    Redis 중복 체크 로직 포함.
    """
    try:
        # 1. Redis 중복 체크
        cache_key = f"missed_noti_sent:{plan.id}"
        if cache.get(cache_key) and not is_force:
            msg = f"⏭️ [스킵] Plan {plan.id}: 이미 알림 전송됨 (Redis 캐시)"
            print(msg)
            return False, msg

        # 2. 보호자 정보 확인
        patient = plan.regihistory.user
        guardian_email = patient.prot_email

        if not guardian_email:
            msg = f"⚠️ [실패] Plan {plan.id} ({patient.username}): 보호자 이메일 없음"
            print(msg)
            return False, msg

        # 3. 보호자 유저 조회 (앱 사용자일 경우)
        guardian = User.objects.filter(email=guardian_email).first()

        # 4. FCM 전송
        if guardian and guardian.fcm_token:
            # 직접 Message 객체를 생성할 때는 초기화가 필수 (위쪽 test_missed_alarm_view에서 호출됨)
            message = messaging.Message(
                notification=messaging.Notification(
                    title="🚨 미복용 알림",
                    body=f"{patient.username}님이 [{plan.med_name}] 약을 아직 복용하지 않았습니다."
                ), data={  # 🔥 여기 추가
                "type": "missed_alarm",
                "plan_id": str(plan.id),
                "user_name": patient.username,
                "med_name": plan.med_name,
            },
                token=guardian.fcm_token,
            )
            messaging.send(message)

            # 5. 캐시 저장 (24시간 동안 유효)
            cache.set(cache_key, "True", timeout=86400)

            log = f"🚀 <b>[전송 성공]</b> 환자:{patient.username} → 보호자:{guardian_email}"
            print(log)
            return True, log
        else:
            msg = f"❌ [실패] 보호자({guardian_email})를 찾을 수 없거나 FCM 토큰 없음"
            print(msg)
            return False, msg

    except Exception as e:
        err = f"🔥 에러 (Plan {plan.id}): {e}"
        print(err)
        return False, err


def _build_response(title, now_kst, success_count, total_count, logs):
    """
    HTML 응답을 생성하는 공통 함수
    """
    html_content = [
        f"<h1>{title} 결과</h1>",
        f"<p><b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}</p>",
        f"<p><b>전송 성공:</b> {success_count}건 / 전체 {total_count}건</p>",
        f"<hr>",
        f"<br>".join(logs)
    ]
    return HttpResponse("".join(html_content))