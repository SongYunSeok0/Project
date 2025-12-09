import logging
from celery import shared_task
from django.contrib.auth import get_user_model
from django.utils import timezone
from datetime import timedelta
from django.core.cache import cache
from firebase_admin import messaging
from notifications.services import send_fcm_to_token, initialize_firebase
from .models import Plan

logger = logging.getLogger("celery")
User = get_user_model()


# ====================================================
# 1. [Celery Task] 환자 정시 복용 알림 (1분마다 실행)
# ====================================================
@shared_task
def send_med_alarms_task():
    """
    1분마다 실행되어, 정확히 현재 시간에 복용해야 할 약(Plan)을 찾아 알림을 전송합니다.
    ⭐ RegiHistory 단위로 그룹화하여 중복 알림 방지
    """
    now_utc = timezone.now()
    now_kst = timezone.localtime(now_utc)

    # 1. 검색 범위: 현재 분 ~ 1분 뒤 (초 단위 절삭)
    start_time = now_utc.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    logger.info(f"[MED] 환자 정시 알림 체크 시작 → {now_kst.strftime('%Y-%m-%d %H:%M')} (KST)")

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

    logger.info(f"[MED] 발견된 Plan: {targets.count()}개, 그룹화된 RegiHistory: {len(regihistory_groups)}개")

    # 4. RegiHistory 단위로 알림 전송
    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        try:
            regihistory = group_data['regihistory']
            plans = group_data['plans']
            earliest_time = group_data['earliest_time']

            if not regihistory or not regihistory.user:
                logger.warning(f"[MED] 데이터 오류 → RegiHistory ID: {regi_id}")
                continue

            user = regihistory.user
            token = getattr(user, 'fcm_token', None)

            if not token:
                logger.warning(f"[MED] FCM 토큰 없음 → user_id={user.id}, username={user.username}")
                continue

            # 한국 시간 변환
            plan_time_str = timezone.localtime(earliest_time).strftime('%H:%M')
            plan_count = len(plans)
            plan_ids = [str(p.id) for p in plans]

            # FCM 전송 (type="ALARM"으로 전체 화면 알림)
            send_fcm_to_token(
                token=token,
                title="💊 약 드실 시간이에요!",
                body=f"{user.username}님, [{regihistory.label}] 복용 시간입니다. ({plan_time_str})",
                data={
                    "type": "ALARM",
                    "regihistory_id": str(regihistory.id),
                    "plan_ids": ",".join(plan_ids),
                    "plan_count": str(plan_count),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                }
            )

            success_count += 1
            logger.info(
                f"[MED] 알림 전송 성공 → user_id={user.id}, regihistory_id={regi_id}, "
                f"plan_count={plan_count}, plan_ids={','.join(plan_ids)}, time={plan_time_str}"
            )

        except Exception as e:
            logger.error(f"[MED] 알림 전송 실패 → regihistory_id={regi_id}, error={e}")

    logger.info(f"[MED] 총 {success_count}개 RegiHistory 그룹에 대한 알림 발송 완료")
    return f"총 {success_count}건 전송 완료 (Plan {targets.count()}개를 {len(regihistory_groups)}개 그룹으로 처리)"


# ====================================================
# 2. [Celery Task] 보호자 미복용 알림 (30분 지연)
# ====================================================
@shared_task
def check_missed_medication():
    """
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

    logger.info(f"[MISSED] 미복용 알림 체크 시작 → {now_kst.strftime('%Y-%m-%d %H:%M')} (KST)")

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

    logger.info(f"[MISSED] 발견된 미복용 Plan: {missed_plans.count()}개, 그룹화된 RegiHistory: {len(regihistory_groups)}개")

    # 4. RegiHistory 단위로 알림 전송
    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        try:
            regihistory = group_data['regihistory']
            plans = group_data['plans']

            # Redis 중복 체크 (RegiHistory ID 기준)
            cache_key = f"missed_noti_sent:regi:{regi_id}"
            if cache.get(cache_key):
                logger.info(f"[MISSED] 스킵 → RegiHistory {regi_id}: 이미 알림 전송됨 (Redis 캐시)")
                continue

            # 환자 정보
            patient = regihistory.user
            guardian_email = patient.prot_email

            if not guardian_email:
                logger.warning(f"[MISSED] 보호자 이메일 없음 → RegiHistory {regi_id}, patient={patient.username}")
                continue

            # 보호자 유저 조회
            guardian = User.objects.filter(email=guardian_email).first()

            if not guardian or not guardian.fcm_token:
                logger.warning(f"[MISSED] 보호자 FCM 토큰 없음 → guardian_email={guardian_email}")
                continue

            # 환자 전화번호 추출 및 포맷팅
            patient_phone = ""
            if hasattr(patient, 'phone'):
                patient_phone = patient.phone or ""
            elif hasattr(patient, 'phone_number'):
                patient_phone = patient.phone_number or ""
            patient_phone = patient_phone.replace('-', '').replace(' ', '')

            # Plan 정보
            med_name = regihistory.label
            plan_count = len(plans)
            plan_id = [str(p.id) for p in plans]

            # FCM 전송 (data만 사용, notification 없음)
            message = messaging.Message(
                data={
                    "type": "missed_alarm",
                    "regihistory_id": str(regihistory.id),
                    "plan_id": ",".join(plan_id),
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

            # 캐시 저장 (24시간) - RegiHistory ID 기준
            cache.set(cache_key, "True", timeout=86400)

            success_count += 1
            logger.info(
                f"[MISSED] 알림 전송 성공 → patient={patient.username}, guardian={guardian.email}, "
                f"regihistory_id={regi_id}, plan_count={plan_count}, fcm_response={response}"
            )

        except Exception as e:
            logger.error(f"[MISSED] 알림 전송 실패 → regihistory_id={regi_id}, error={e}", exc_info=True)

    logger.info(f"[MISSED] 총 {success_count}개 RegiHistory 그룹에 대한 미복용 알림 발송 완료")
    return f"미복용 체크 완료: {success_count}건 발송 (Plan {missed_plans.count()}개를 {len(regihistory_groups)}개 그룹으로 처리)"

@shared_task
def delete_plan_async(plan_id: int, user_id: int):
    """
    Plan 삭제를 비동기 처리
    """
    try:
        plan = Plan.objects.filter(id=plan_id, regihistory__user_id=user_id).first()
        if not plan:
            return f"Plan {plan_id} not found or no permission"

        plan.delete()
        return f"Plan {plan_id} deleted successfully"

    except Exception as e:
        return f"Error deleting Plan {plan_id}: {e}"