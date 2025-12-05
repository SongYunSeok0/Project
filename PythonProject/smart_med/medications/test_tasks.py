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
# 1. [View] 환자 정시 복용 알림 postman용 테스트 (그룹화 적용)
# ====================================================
def test_med_alarm_view(request):
    """
    [테스트용] tasks.py의 send_med_alarms_task 로직을 수동 실행합니다.
    현재 분(minute)에 복용해야 할 약을 찾아 환자에게 전체화면 알람(ALARM)을 보냅니다.
    (동일 처방, 동일 시간은 그룹화하여 1건만 발송)
    """

    # Firebase 초기화 (안전장치)
    initialize_firebase()

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

    # 3. ⭐ [핵심] 그룹화 로직 적용
    grouped_plans = {}
    for plan in targets:
        # 같은 유저, 같은 처방(그룹), 같은 시간이라면 하나로 묶음
        key = (plan.regihistory.user.id, plan.regihistory.id, plan.taken_at)

        # 딕셔너리에 없으면 최초 등록 (이 녀석이 대표가 됨)
        if key not in grouped_plans:
            grouped_plans[key] = plan

    # 4. 결과 로그 초기화
    total_raw_count = targets.count()
    total_group_count = len(grouped_plans)

    result_log = [
        f"<b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>",
        f"<b>검색 범위(UTC):</b> {start_time.strftime('%H:%M')} ~ {end_time.strftime('%H:%M')}<br>",
        f"<b>검색된 약 개수:</b> {total_raw_count}개 → <b>그룹화 후:</b> {total_group_count}건<hr>"
    ]

    count = 0

    if total_group_count == 0:
        msg = "⚠️ 현재 시간에 복용해야 할 약이 없습니다."
        print(msg)
        result_log.append(msg)
    else:
        # 5. 그룹별 대표 플랜으로 알림 전송
        for plan in grouped_plans.values():
            success, log_msg = _process_regular_alarm(plan)
            result_log.append(log_msg)
            if success:
                count += 1

    print(f"=== [TEST Regular] 종료: {count}건 전송 (그룹화 적용됨) ===\n")
    return _build_response("🔔 환자 정시 알림 테스트 (그룹화)", now_kst, count, total_group_count, result_log)


# ====================================================
# 2. [View] 보호자 미복용 알림 테스트 (30분 지연) - 그룹화 적용됨
# ====================================================
def test_missed_alarm_view(request):
    """
    [테스트용] tasks.py의 check_missed_medication 로직을 수동 실행합니다.
    30분이 지났는데 미복용(taken is NULL)인 건에 대해 보호자에게 알림을 보냅니다.
    (동일 처방, 동일 시간은 그룹화하여 1건만 발송)
    """
    initialize_firebase()

    now = timezone.now()
    now_kst = timezone.localtime(now)

    # 1. 검색 범위: 30분 전 ~ 24시간 전
    end_time = now - timedelta(minutes=1)
    start_time = now - timedelta(days=1)

    print(f"\n=== [TEST Missed] 미복용(보호자) 체크 시작 ({now_kst.strftime('%H:%M')}) ===")

    # 2. 데이터 조회 (아직 안 먹은 약만)
    missed_plans = Plan.objects.filter(
        taken_at__range=(start_time, end_time),
        taken__isnull=True
    ).select_related('regihistory__user')

    # 3. ⭐ [추가됨] 미복용 알림 그룹화 로직 적용
    grouped_missed_plans = {}
    for plan in missed_plans:
        # Key: (유저ID, 처방ID, 복용예정시간)
        # 같은 시간에 먹어야 하는 약들은 하나의 알림으로 취급
        key = (plan.regihistory.user.id, plan.regihistory.id, plan.taken_at)

        if key not in grouped_missed_plans:
            grouped_missed_plans[key] = plan

    # 4. 결과 로그 초기화
    total_raw_count = missed_plans.count()
    total_group_count = len(grouped_missed_plans)

    result_log = [
        f"<b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>",
        f"<b>검색 범위:</b> 30분 전 ~ 24시간 전<br>",
        f"<b>검색된 미복용 약 개수:</b> {total_raw_count}개 → <b>그룹화 후(전송 대상):</b> {total_group_count}건<hr>"
    ]

    count = 0

    # URL 파라미터 ?force=true 가 있으면 캐시 무시하고 강제 전송
    is_force = request.GET.get('force') == 'true'

    if total_group_count == 0:
        msg = "✅ 미복용 상태인 건이 없거나, 아직 30분이 지나지 않았습니다."
        print(msg)
        result_log.append(msg)
    else:
        # 5. 그룹별 대표 플랜으로 알림 전송 (반복 대상 변경: missed_plans -> grouped_missed_plans.values())
        for plan in grouped_missed_plans.values():
            success, log_msg = _process_missed_alarm(plan, is_force)
            result_log.append(log_msg)
            if success:
                count += 1

    print(f"=== [TEST Missed] 종료: {count}건 전송 (그룹화 적용됨) ===\n")
    return _build_response("🚨 미복용 알림(보호자) 테스트", now_kst, count, total_group_count, result_log)


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

        # 그룹명(처방명) 사용. 없으면 약 이름 사용
        label = plan.regihistory.label if plan.regihistory and plan.regihistory.label else plan.med_name

        if token:
            # ⭐ 핵심: type="ALARM"으로 보내서 전체 화면 알림 트리거
            # send_fcm_to_token 내부에서 initialize_firebase()를 수행하므로 안전함

            # AppFcmService에서 필요한 상세 정보들 추가
            data_payload = {
                "type": "ALARM",
                "plan_id": str(plan.id),
                "click_action": "FLUTTER_NOTIFICATION_CLICK",
                # 상세 정보 추가
                "user_name": user.username,
                "med_name": label,  # 약 이름 대신 그룹명 전달
                "taken_at": plan_time_str,
                "meal_time": plan.meal_time or "",
                "note": plan.note or ""
            }

            send_fcm_to_token(
                token=token,
                title="💊 약 드실 시간이에요!",
                body=f"{user.username}님, [{label}] 복용 시간입니다. ({plan_time_str})",
                data=data_payload
            )
            log = f"✅ <b>[전송 성공]</b> {user.username} / {label} ({plan_time_str})"
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
        # 그룹화된 경우 대표 Plan ID 하나만 체크하면, 나머지 같은 그룹은 자연스럽게 처리된 것으로 간주됩니다.
        cache_key = f"missed_noti_sent:{plan.id}"
        if cache.get(cache_key) and not is_force:
            msg = f"⏭️ [스킵] Plan {plan.id} (그룹 대표): 이미 알림 전송됨 (Redis 캐시)"
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

        label = plan.regihistory.label if plan.regihistory and plan.regihistory.label else plan.med_name
        plan_time_str = timezone.localtime(plan.taken_at).strftime('%H:%M')

        # 4. FCM 전송
        if guardian and guardian.fcm_token:
            # 직접 Message 객체를 생성할 때는 초기화가 필수 (위쪽 test_missed_alarm_view에서 호출됨)
            message = messaging.Message(
                data={
                    "type": "missed_alarm",
                    "plan_id": str(plan.id),
                    "user_name": patient.username,
                    "med_name": label,
                    "taken_at": plan_time_str,
                    "is_guardian": "true"
                },
                notification=messaging.Notification(
                    title="🚨 미복용 알림",
                    body=f"{patient.username}님이 [{label}] 약을 아직 복용하지 않았습니다."
                ),
                token=guardian.fcm_token,
            )
            messaging.send(message)

            # 5. 캐시 저장 (24시간 동안 유효)
            # 대표 플랜 ID를 저장하여 다음 실행 시 동일 그룹(동일 처방, 동일 시간)의 중복 전송 방지
            cache.set(cache_key, "True", timeout=86400)

            log = f"🚀 <b>[전송 성공]</b> 환자:{patient.username} → 보호자:{guardian_email} (Plan {plan.id})"
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
        f"<p><b>전송 성공:</b> {success_count}건 / 전체 {total_count}건 (그룹화됨)</p>",
        f"<hr>",
        f"<br>".join(logs)
    ]
    return HttpResponse("".join(html_content))