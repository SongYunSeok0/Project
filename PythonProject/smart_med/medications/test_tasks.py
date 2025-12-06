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
    ⭐ RegiHistory 단위로 그룹화하여 중복 알림 방지
    """
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

    # 3. RegiHistory 단위로 그룹화
    regihistory_groups = {}
    for plan in targets:
        if plan.regihistory:
            regi_id = plan.regihistory.id
            if regi_id not in regihistory_groups:
                regihistory_groups[regi_id] = {
                    'regihistory': plan.regihistory,
                    'plans': [],
                    'earliest_time': plan.taken_at
                }
            regihistory_groups[regi_id]['plans'].append(plan)
            # 가장 빨른 복용 시간 저장
            if plan.taken_at < regihistory_groups[regi_id]['earliest_time']:
                regihistory_groups[regi_id]['earliest_time'] = plan.taken_at

    # 4. 결과 로그 초기화
    result_log = [
        f"<b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>",
        f"<b>검색 범위(UTC):</b> {start_time.strftime('%H:%M')} ~ {end_time.strftime('%H:%M')}<br>",
        f"<b>발견된 Plan 수:</b> {targets.count()}개<br>",
        f"<b>그룹화된 RegiHistory 수:</b> {len(regihistory_groups)}개<hr>"
    ]

    count = 0
    total_count = len(regihistory_groups)

    if total_count == 0:
        msg = "⚠️ 현재 시간에 복용해야 할 약이 없습니다."
        print(msg)
        result_log.append(msg)
    else:
        # 5. RegiHistory 단위로 알림 전송
        for regi_id, group_data in regihistory_groups.items():
            success, log_msg = _process_regular_alarm_grouped(
                group_data['regihistory'],
                group_data['plans'],
                group_data['earliest_time']
            )
            result_log.append(log_msg)
            if success:
                count += 1

    print(f"=== [TEST Regular] 종료: {count}건 전송 (총 {len(regihistory_groups)}개 그룹) ===\n")
    return _build_response("🔔 환자 정시 알림 테스트", now_kst, count, total_count, result_log)


# ====================================================
# 2. [View] 보호자 미복용 알림 테스트 (30분 지연)
# ====================================================
def test_missed_alarm_view(request):
    """
    [테스트용] tasks.py의 check_missed_medication 로직을 수동 실행합니다.
    30분이 지났는데 미복용(taken is NULL)인 건에 대해 보호자에게 알림을 보냅니다.
    ⭐ RegiHistory 단위로 그룹화하여 중복 알림 방지
    """
    # Firebase 초기화
    initialize_firebase()

    now = timezone.now()
    now_kst = timezone.localtime(now)

    # 1. 검색 범위: 30분 전 ~ 24시간 전
    end_time = now - timedelta(minutes=30)
    start_time = now - timedelta(days=1)

    print(f"\n=== [TEST Missed] 미복용(보호자) 체크 시작 ({now_kst.strftime('%H:%M')}) ===")

    # 2. 데이터 조회 (아직 안 먹은 약만)
    missed_plans = Plan.objects.filter(
        taken_at__range=(start_time, end_time),
        taken__isnull=True
    ).select_related('regihistory__user')

    # 3. RegiHistory 단위로 그룹화
    regihistory_groups = {}
    for plan in missed_plans:
        if plan.regihistory:
            regi_id = plan.regihistory.id
            if regi_id not in regihistory_groups:
                regihistory_groups[regi_id] = {
                    'regihistory': plan.regihistory,
                    'plans': [],
                    'earliest_time': plan.taken_at
                }
            regihistory_groups[regi_id]['plans'].append(plan)
            # 가장 오래된 미복용 시간 저장
            if plan.taken_at < regihistory_groups[regi_id]['earliest_time']:
                regihistory_groups[regi_id]['earliest_time'] = plan.taken_at

    # 4. 결과 로그 초기화
    result_log = [
        f"<b>서버 시간(KST):</b> {now_kst.strftime('%Y-%m-%d %H:%M:%S')}<br>",
        f"<b>검색 범위:</b> 30분 전 ~ 24시간 전<br>",
        f"<b>발견된 미복용 Plan 수:</b> {missed_plans.count()}개<br>",
        f"<b>그룹화된 RegiHistory 수:</b> {len(regihistory_groups)}개<hr>"
    ]

    count = 0
    total_count = len(regihistory_groups)

    # URL 파라미터 ?force=true 가 있으면 캐시 무시하고 강제 전송
    is_force = request.GET.get('force') == 'true'

    if total_count == 0:
        msg = "✅ 미복용 상태인 건이 없거나, 아직 30분이 지나지 않았습니다."
        print(msg)
        result_log.append(msg)
    else:
        # 5. RegiHistory 단위로 알림 전송
        for regi_id, group_data in regihistory_groups.items():
            success, log_msg = _process_missed_alarm_grouped(
                group_data['regihistory'],
                group_data['plans'],
                is_force
            )
            result_log.append(log_msg)
            if success:
                count += 1

    print(f"=== [TEST Missed] 종료: {count}건 전송 (총 {len(regihistory_groups)}개 그룹) ===\n")
    return _build_response("🚨 미복용 알림(보호자) 테스트", now_kst, count, total_count, result_log)


# ====================================================
# 3. [Helper] 내부 로직 함수들 (실제 기능 수행)
# ====================================================

def _process_regular_alarm_grouped(regihistory, plans, earliest_time):
    """
    환자 정시 알림을 RegiHistory 단위로 처리합니다. (type="ALARM")
    같은 RegiHistory에 속한 여러 Plan을 하나의 알림으로 통합합니다.
    """
    try:
        if not regihistory or not regihistory.user:
            return False, f"⚠️ 데이터 오류 (RegiHistory {regihistory.id if regihistory else 'None'})"

        user = regihistory.user
        token = getattr(user, 'fcm_token', None)

        # 한국 시간 변환 (로그 및 메시지용)
        plan_time_str = timezone.localtime(earliest_time).strftime('%H:%M')

        # Plan 개수 정보
        plan_count = len(plans)
        plan_ids = [str(p.id) for p in plans]

        if token:
            # ⭐ 핵심: type="ALARM"으로 보내서 전체 화면 알림 트리거
            # 여러 Plan을 하나의 알림으로 통합
            send_fcm_to_token(
                token=token,
                title="💊 약 드실 시간이에요!",
                body=f"{user.username}님, [{regihistory.label}] 복용 시간입니다. ({plan_time_str})",
                data={
                    "type": "ALARM",
                    "regihistory_id": str(regihistory.id),
                    "plan_ids": ",".join(plan_ids),  # 여러 Plan ID를 쉼표로 구분
                    "plan_count": str(plan_count),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                }
            )
            log = f"✅ <b>[전송 성공]</b> {user.username} / {regihistory.label} ({plan_time_str}) | Plan 수: {plan_count}개 (ID: {', '.join(plan_ids)})"
            print(log)
            return True, log
        else:
            log = f"❌ [실패] {user.username}: FCM 토큰 없음 | RegiHistory ID: {regihistory.id}"
            print(log)
            return False, log

    except Exception as e:
        err = f"⚠️ 에러 (RegiHistory {regihistory.id if regihistory else 'None'}): {e}"
        print(err)
        return False, err


def _process_missed_alarm_grouped(regihistory, plans, is_force=False):
    """
    보호자 미복용 알림을 RegiHistory 단위로 처리합니다.
    Redis 중복 체크 로직 포함 (RegiHistory ID 기준).
    """
    try:
        # 1. Redis 중복 체크 (RegiHistory ID 기준)
        cache_key = f"missed_noti_sent:regi:{regihistory.id}"
        if cache.get(cache_key) and not is_force:
            msg = f"⏭️ [스킵] RegiHistory {regihistory.id}: 이미 알림 전송됨 (Redis 캐시)"
            print(msg)
            return False, msg

        # 2. 환자 정보
        patient = regihistory.user
        guardian_email = patient.prot_email

        if not guardian_email:
            msg = f"⚠️ [실패] RegiHistory {regihistory.id} ({patient.username}): 보호자 이메일 없음"
            print(msg)
            return False, msg

        # 3. 보호자 유저 조회
        guardian = User.objects.filter(email=guardian_email).first()

        if not guardian or not guardian.fcm_token:
            msg = f"❌ [실패] 보호자({guardian_email})를 찾을 수 없거나 FCM 토큰 없음"
            print(msg)
            return False, msg

        # 4. 환자 전화번호 추출
        patient_phone = ""
        if hasattr(patient, 'phone'):
            patient_phone = patient.phone or ""
        elif hasattr(patient, 'phone_number'):
            patient_phone = patient.phone_number or ""

        # 전화번호 포맷팅 (하이픈 제거)
        patient_phone = patient_phone.replace('-', '').replace(' ', '')

        print(f"📞 환자: {patient.username}, 전화번호: {patient_phone}")

        # 5. 약 이름 및 Plan 정보
        med_name = regihistory.label
        plan_count = len(plans)
        plan_ids = [str(p.id) for p in plans]

        # 6. FCM 전송 (data만 사용, notification 없음)
        print(f"🚀 FCM 전송 시작 - 보호자 토큰: {guardian.fcm_token[:20]}...")

        message = messaging.Message(
            data={
                "type": "missed_alarm",
                "regihistory_id": str(regihistory.id),
                "plan_ids": ",".join(plan_ids),
                "plan_count": str(plan_count),
                "user_name": patient.username,
                "med_name": med_name,
                "patient_phone": patient_phone,
                "title": "🚨 미복용 알림",
                "body": f"{patient.username}님이 [{med_name}] 약을 아직 복용하지 않았습니다."
            },
            token=guardian.fcm_token,
        )

        response = messaging.send(message)
        print(f"✅ FCM 응답: {response}")

        # 7. 캐시 저장 (24시간) - RegiHistory ID 기준
        cache.set(cache_key, "True", timeout=86400)

        log = f"🚀 <b>[전송 성공]</b> 환자:{patient.username} (📞{patient_phone}) → 보호자:{guardian.email} | RegiHistory: {regihistory.id}, Plan 수: {plan_count}개"
        print(log)
        return True, log

    except Exception as e:
        err = f"🔥 에러 (RegiHistory {regihistory.id if regihistory else 'None'}): {e}"
        print(err)
        import traceback
        traceback.print_exc()
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