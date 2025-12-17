import logging
from celery import shared_task
from django.contrib.auth import get_user_model
from django.utils import timezone
from django.db.models import Q
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
    ⭐ 수정사항: send_fcm_to_token 대신 messaging.Message를 직접 사용하여
               'priority=high'를 설정함 (절전모드에서도 화면 깨우기 위함)
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
            # 가장 빠른 복용 시간 저장
            if plan.taken_at < regihistory_groups[regi_id]['earliest_time']:
                regihistory_groups[regi_id]['earliest_time'] = plan.taken_at

    logger.info(f"[MED] 발견된 Plan: {targets.count()}개, 그룹화된 RegiHistory: {len(regihistory_groups)}개")

    # 4. RegiHistory 단위로 알림 전송 (High Priority 적용)
    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        try:
            regihistory = group_data['regihistory']
            plans = group_data['plans']
            earliest_time = group_data['earliest_time']

            if not regihistory or not regihistory.user:
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

            # 🔴 [핵심 수정] Android High Priority 설정
            # 화면이 꺼져 있거나 절전 모드일 때 즉시 깨우기 위한 필수 설정입니다.
            message = messaging.Message(
                token=token,
                data={
                    "type": "ALARM",
                    "title": "💊 약 드실 시간이에요!",
                    "body": f"{user.username}님, [{regihistory.label}] 복용 시간입니다. ({plan_time_str})",
                    "regihistory_id": str(regihistory.id),
                    "plan_ids": ",".join(plan_ids),
                    "plan_count": str(plan_count),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                },
                # 👇 안드로이드 설정: 중요도 높음, 즉시 전송
                android=messaging.AndroidConfig(
                    priority='high',
                    ttl=0,  # 지연 없이 즉시 배달
                ),
                # 👇 iOS 설정: 백그라운드 깨우기
                apns=messaging.APNSConfig(
                    payload=messaging.APNSPayload(
                        aps=messaging.Aps(content_available=True)
                    )
                )
            )

            # 전송
            response = messaging.send(message)

            success_count += 1
            logger.info(
                f"[MED] 알림 전송 성공(High Priority) → user_id={user.id}, regihistory_id={regi_id}, "
                f"time={plan_time_str}, response={response}"
            )

        except Exception as e:
            logger.error(f"[MED] 알림 전송 실패 → regihistory_id={regi_id}, error={e}")

    logger.info(f"[MED] 총 {success_count}개 RegiHistory 그룹에 대한 알림 발송 완료")
    return f"총 {success_count}건 전송 완료"

# ====================================================
# 2. [Celery Task] 보호자 미복용 알림 (30분 지연)
# ====================================================
@shared_task
def check_missed_medication():
    """
    30분이 지났는데 미복용(taken is NULL)인 건에 대해 보호자에게 알림을 보냅니다.
    ⭐ RegiHistory + 복용시간(earliest_time)을 조합하여 중복 알림을 방지합니다.
    """
    # Firebase 초기화 (안전장치)
    initialize_firebase()

    now = timezone.now()
    now_kst = timezone.localtime(now)

    # 1. 검색 범위 설정
    # 현재 시각보다 30분 전 ~ 24시간 전 사이의 약만 조회
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

            # 그룹 내에서 가장 오래된(빠른) 미복용 시간 업데이트
            if plan.taken_at < regihistory_groups[regi_id]['earliest_time']:
                regihistory_groups[regi_id]['earliest_time'] = plan.taken_at

    logger.info(f"[MISSED] 발견된 미복용 Plan: {missed_plans.count()}개, 그룹화된 RegiHistory: {len(regihistory_groups)}개")

    # 4. RegiHistory 단위로 알림 전송
    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        try:
            regihistory = group_data['regihistory']
            plans = group_data['plans']

            # 🟢 [수정 포인트] 딕셔너리에서 시간을 꺼내와야 합니다.
            earliest_time = group_data['earliest_time']

            # 🟢 [Redis 중복 체크 키 생성]
            # 키 형식: missed_noti:regi:{ID}:time:{YYYYMMDDHHMM}
            # 약의 종류(ID)와 복용해야 했던 시간(Time)이 모두 같아야만 중복으로 처리
            time_key = earliest_time.strftime("%Y%m%d%H%M")
            cache_key = f"missed_noti_sent:regi:{regi_id}:time:{time_key}"

            # 이미 전송된 기록이 있다면 건너뜀
            if cache.get(cache_key):
                # logger.info(f"[MISSED] 스킵 → 이미 전송됨 (RegiID: {regi_id}, Time: {time_key})")
                continue

            # --- 이하 전송 로직 ---
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

            # 메시지 내용 구성
            med_name = regihistory.label
            plan_count = len(plans)
            plan_ids = [str(p.id) for p in plans]

            # 메시지에 표시할 시간 (한국 시간 기준)
            missed_time_str = timezone.localtime(earliest_time).strftime('%H:%M')

            # FCM 전송
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
                    "body": f"{patient.username}님이 [{med_name}] 약을 아직 복용하지 않았습니다. ({missed_time_str})"
                },
                token=guardian.fcm_token,
            )

            response = messaging.send(message)

            # 🟢 [중요] 전송 성공 시 캐시 저장 (24시간 동안 유지)
            cache.set(cache_key, "True", timeout=86400)

            success_count += 1
            logger.info(
                f"[MISSED] 알림 전송 성공 → patient={patient.username}, guardian={guardian.email}, "
                f"regihistory_id={regi_id}, time={missed_time_str}"
            )

        except Exception as e:
            logger.error(f"[MISSED] 알림 전송 실패 → regihistory_id={regi_id}, error={e}", exc_info=True)

    logger.info(f"[MISSED] 총 {success_count}개 그룹에 대한 알림 발송 완료")
    return f"미복용 체크 완료: {success_count}건 발송"


# ====================================================
# 3. [Celery Task] 환자 재알림 통합 (10분 & 20분 경과)
# ====================================================
@shared_task
def send_user_reminders_task():
    """
    현재 시간 기준으로 복용 시간이 '10분 전' 또는 '20분 전'인 약을 찾아
    환자 본인에게 재알림을 보냅니다. (아직 안 먹은 경우만)
    """
    # 1. 시간 설정
    now_utc = timezone.now()  # DB 조회용 (UTC)
    now_kst = timezone.localtime(now_utc)  # 로그 출력용 (KST)

    # (선택사항) 작업 시작 로그를 한국 시간으로 찍어두면 디버깅이 편합니다.
    # logger.info(f"[REMINDER] 재알림 체크 시작 → {now_kst.strftime('%Y-%m-%d %H:%M:%S')} (KST)")

    # 2. 시간대 계산 (초 단위 절삭) - UTC 기준
    # Target 1: 10분 전
    time_10_start = (now_utc - timedelta(minutes=10)).replace(second=0, microsecond=0)
    time_10_end = time_10_start + timedelta(minutes=1)

    # Target 2: 20분 전
    time_20_start = (now_utc - timedelta(minutes=20)).replace(second=0, microsecond=0)
    time_20_end = time_20_start + timedelta(minutes=1)

    # 3. 데이터 조회 (OR 조건 사용: 10분 전 범위 이거나 OR 20분 전 범위)
    targets = Plan.objects.filter(
        Q(taken_at__range=(time_10_start, time_10_end)) |
        Q(taken_at__range=(time_20_start, time_20_end)),
        use_alarm=True,
        taken__isnull=True  # 미복용만
    ).select_related('regihistory__user')

    # 4. RegiHistory 그룹화
    regihistory_groups = {}
    for plan in targets:
        if not plan.regihistory: continue

        regi_id = plan.regihistory.id
        if regi_id not in regihistory_groups:
            regihistory_groups[regi_id] = {
                'regihistory': plan.regihistory,
                'plans': [],
                'earliest_time': plan.taken_at
            }
        regihistory_groups[regi_id]['plans'].append(plan)

    # 5. 알림 전송 및 메시지 분기 처리
    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        try:
            regihistory = group_data['regihistory']
            plans = group_data['plans']
            earliest_time = group_data['earliest_time']
            user = regihistory.user
            token = getattr(user, 'fcm_token', None)

            if not token: continue

            # 몇 분 지났는지 계산
            diff_minutes = (now_utc - earliest_time).total_seconds() / 60

            # 메시지 내용 분기
            if 10 <= diff_minutes <= 11:
                # 10분 경과
                title = "💊 [재알림] 약 드셨나요?"
                body = f"{user.username}님, [{regihistory.label}] 복용 시간 10분이 지났습니다. 잊지 말고 챙겨드세요!"
                log_prefix = "10분 경과"
            elif 20 <= diff_minutes <= 21:
                # 20분 경과
                title = "💊 [2차 알림] 약 복용 잊으셨나요?"
                body = f"{user.username}님, [{regihistory.label}] 복용 시간 20분이 지났습니다. 건강을 위해 지금 복용해주세요."
                log_prefix = "20분 경과"
            else:
                continue

            plan_ids = [str(p.id) for p in plans]

            message = messaging.Message(
                token=token,
                data={
                    "type": "ALARM",
                    "title": title,
                    "body": body,
                    "regihistory_id": str(regihistory.id),
                    "plan_ids": ",".join(plan_ids),
                    "plan_count": str(len(plans)),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                },
                android=messaging.AndroidConfig(priority='high', ttl=0),
                apns=messaging.APNSConfig(payload=messaging.APNSPayload(aps=messaging.Aps(content_available=True)))
            )
            messaging.send(message)
            success_count += 1

            # ✅ [수정됨] 로그에 한국 시간(now_kst) 표시
            logger.info(
                f"[REMINDER] {log_prefix} 알림 전송 완료 → "
                f"Time: {now_kst.strftime('%H:%M')} (KST), User: {user.username}"
            )

        except Exception as e:
            logger.error(f"[REMINDER] 전송 실패 error={e}")

    # 여기도 한국 시간으로 로그 남기기 (선택사항)
    # if success_count > 0:
    #     logger.info(f"[REMINDER] 총 {success_count}건 전송 완료 at {now_kst.strftime('%H:%M')} (KST)")

    return f"재알림(10/20분) 통합 체크 완료: {success_count}건 전송"

@shared_task
def delete_plan_async(plan_id):
    """비동기로 Plan 삭제"""
    try:
        plan = Plan.objects.get(id=plan_id)
        plan.delete()
        return f"Plan {plan_id} deleted successfully"
    except Plan.DoesNotExist:
        return f"Plan {plan_id} not found"