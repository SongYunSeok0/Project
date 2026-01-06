import logging
from celery import shared_task
from django.contrib.auth import get_user_model
from django.utils import timezone
from django.db.models import Q
from datetime import timedelta
from django.core.cache import cache
from firebase_admin import messaging
from notifications.services import send_fcm_to_token, initialize_firebase
# Notification 모델을 models에서 import 해야 합니다.
from .models import Plan
from notifications.models import Notification

logger = logging.getLogger("celery")
User = get_user_model()


# ====================================================
# [Helper] 알림 로그 저장 함수 (내부 사용용)
# ====================================================
def _save_notification_log(regihistory, noti_type, status, title=None, body=None, metadata=None, error_msg=None):
    """
    알림 전송 결과를 Notification 테이블에 저장합니다.
    오류가 발생해도 메인 로직(알림 전송 등)이 멈추지 않도록 예외 처리합니다.
    """
    try:
        full_metadata = metadata or {}
        if title: full_metadata['title'] = title
        if body: full_metadata['body'] = body

        Notification.objects.create(
            regihistory=regihistory,
            notification_type=noti_type,
            status=status,
            sent_at=timezone.now(),
            error_message=str(error_msg) if error_msg else None,
            metadata=full_metadata
        )
    except Exception as e:
        logger.error(f"[LOG_ERROR] Notification 로그 저장 실패: {e}")


# ====================================================
# 1. [Celery Task] 환자 정시 복용 알림 (1분마다 실행)
# ====================================================
@shared_task
def send_med_alarms_task():
    now_utc = timezone.now()
    now_kst = timezone.localtime(now_utc)
    start_time = now_utc.replace(second=0, microsecond=0)
    end_time = start_time + timedelta(minutes=1)

    logger.info(f"[MED] 환자 정시 알림 체크 시작 → {now_kst.strftime('%Y-%m-%d %H:%M')} (KST)")

    targets = Plan.objects.filter(
        use_alarm=True,
        taken_at__gte=start_time,
        taken_at__lt=end_time
    ).select_related('regihistory__user')

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
            if plan.taken_at < regihistory_groups[regi_id]['earliest_time']:
                regihistory_groups[regi_id]['earliest_time'] = plan.taken_at

    logger.info(f"[MED] 발견된 Plan: {targets.count()}개, 그룹화된 RegiHistory: {len(regihistory_groups)}개")

    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        regihistory = group_data['regihistory']

        # 메타데이터 준비 (로그 저장용)
        log_title = "💊 약 드실 시간이에요!"
        log_body = ""

        try:
            plans = group_data['plans']
            earliest_time = group_data['earliest_time']

            if not regihistory or not regihistory.user:
                continue

            user = regihistory.user
            token = getattr(user, 'fcm_token', None)

            if not token:
                _save_notification_log(regihistory, "REGULAR_ALARM", "FAILED", error_msg="FCM Token Missing")
                logger.warning(f"[MED] FCM 토큰 없음 → user_id={user.id}")
                continue

            plan_time_str = timezone.localtime(earliest_time).strftime('%H:%M')
            plan_count = len(plans)
            plan_ids = [str(p.id) for p in plans]

            # 본문 내용 생성
            log_body = f"{user.username}님, [{regihistory.label}] 복용 시간입니다. ({plan_time_str})"

            message = messaging.Message(
                token=token,
                data={
                    "type": "ALARM",
                    "title": log_title,
                    "body": log_body,
                    "regihistory_id": str(regihistory.id),
                    "plan_ids": ",".join(plan_ids),
                    "plan_count": str(plan_count),
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                },
                android=messaging.AndroidConfig(priority='high', ttl=0),
                apns=messaging.APNSConfig(payload=messaging.APNSPayload(aps=messaging.Aps(content_available=True)))
            )

            response = messaging.send(message)
            success_count += 1

            # ✅ [성공 로그 저장]
            _save_notification_log(
                regihistory, "REGULAR_ALARM", "SUCCESS",
                title=log_title, body=log_body,
                metadata={"plan_count": plan_count, "plan_ids": plan_ids, "fcm_response": response}
            )

            logger.info(f"[MED] 알림 전송 성공(High Priority) → user_id={user.id}, regihistory_id={regi_id}")

        except Exception as e:
            # ❌ [실패 로그 저장]
            _save_notification_log(
                regihistory, "REGULAR_ALARM", "FAILED",
                title=log_title, body=log_body,
                error_msg=str(e)
            )
            logger.error(f"[MED] 알림 전송 실패 → regihistory_id={regi_id}, error={e}")

    return f"총 {success_count}건 전송 완료"


# ====================================================
# 2. [Celery Task] 보호자 미복용 알림 (30분 지연)
# ====================================================
@shared_task
def check_missed_medication():
    initialize_firebase()
    now = timezone.now()
    end_time = now - timedelta(minutes=30)
    start_time = now - timedelta(days=1)

    logger.info(f"[MISSED] 미복용 알림 체크 시작")

    missed_plans = Plan.objects.filter(
        taken_at__range=(start_time, end_time),
        taken__isnull=True
    ).select_related('regihistory__user')

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
            if plan.taken_at < regihistory_groups[regi_id]['earliest_time']:
                regihistory_groups[regi_id]['earliest_time'] = plan.taken_at

    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        regihistory = group_data['regihistory']

        # 로그용 변수 초기화
        log_title = "🚨 미복용 알림"
        log_body = ""

        try:
            plans = group_data['plans']
            earliest_time = group_data['earliest_time']
            time_key = earliest_time.strftime("%Y%m%d%H%M")
            cache_key = f"missed_noti_sent:regi:{regi_id}:time:{time_key}"

            if cache.get(cache_key):
                continue

            patient = regihistory.user
            guardian_email = patient.prot_email

            if not guardian_email:
                # 보호자 없음 로그 (선택사항, 필요 없으면 제거 가능)
                # _save_notification_log(regihistory, "MISSED_ALARM", "FAILED", error_msg="No Guardian Email")
                continue

            guardian = User.objects.filter(email=guardian_email).first()

            if not guardian or not guardian.fcm_token:
                _save_notification_log(regihistory, "MISSED_ALARM", "FAILED",
                                       error_msg="Guardian Not Found or No Token")
                continue

            patient_phone = ""
            if hasattr(patient, 'phone'):
                patient_phone = patient.phone or ""
            elif hasattr(patient, 'phone_number'):
                patient_phone = patient.phone_number or ""
            patient_phone = patient_phone.replace('-', '').replace(' ', '')

            med_name = regihistory.label
            plan_count = len(plans)
            plan_ids = [str(p.id) for p in plans]
            missed_time_str = timezone.localtime(earliest_time).strftime('%H:%M')

            log_body = f"{patient.username}님이 [{med_name}] 약을 아직 복용하지 않았습니다. ({missed_time_str})"

            message = messaging.Message(
                data={
                    "type": "missed_alarm",
                    "regihistory_id": str(regihistory.id),
                    "plan_ids": ",".join(plan_ids),
                    "plan_count": str(plan_count),
                    "user_name": patient.username,
                    "med_name": med_name,
                    "patient_phone": patient_phone,
                    "title": log_title,
                    "body": log_body
                },
                token=guardian.fcm_token,
            )

            response = messaging.send(message)
            cache.set(cache_key, "True", timeout=86400)
            success_count += 1

            # ✅ [성공 로그 저장]
            _save_notification_log(
                regihistory, "MISSED_ALARM", "SUCCESS",
                title=log_title, body=log_body,
                metadata={
                    "guardian_email": guardian_email,
                    "missed_time": missed_time_str,
                    "fcm_response": response
                }
            )

            logger.info(f"[MISSED] 전송 완료 → patient={patient.username}, guardian={guardian.email}")

        except Exception as e:
            # ❌ [실패 로그 저장]
            _save_notification_log(
                regihistory, "MISSED_ALARM", "FAILED",
                title=log_title, body=log_body,
                error_msg=str(e)
            )
            logger.error(f"[MISSED] 전송 실패 → regihistory_id={regi_id}, error={e}", exc_info=True)

    return f"미복용 체크 완료: {success_count}건 발송"


# ====================================================
# 3. [Celery Task] 환자 재알림 통합 (10분 & 20분 경과)
# ====================================================
@shared_task
def send_user_reminders_task():
    now_utc = timezone.now()
    now_kst = timezone.localtime(now_utc)

    time_10_start = (now_utc - timedelta(minutes=10)).replace(second=0, microsecond=0)
    time_10_end = time_10_start + timedelta(minutes=1)
    time_20_start = (now_utc - timedelta(minutes=20)).replace(second=0, microsecond=0)
    time_20_end = time_20_start + timedelta(minutes=1)

    targets = Plan.objects.filter(
        Q(taken_at__range=(time_10_start, time_10_end)) |
        Q(taken_at__range=(time_20_start, time_20_end)),
        use_alarm=True,
        taken__isnull=True
    ).select_related('regihistory__user')

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

    success_count = 0
    for regi_id, group_data in regihistory_groups.items():
        regihistory = group_data['regihistory']

        # 로그 변수
        log_title = ""
        log_body = ""
        log_type = "REMINDER_UNKNOWN"

        try:
            plans = group_data['plans']
            earliest_time = group_data['earliest_time']
            user = regihistory.user
            token = getattr(user, 'fcm_token', None)

            if not token:
                # 토큰 없으면 실패 로그 저장 후 스킵
                _save_notification_log(regihistory, "REMINDER_FAIL", "FAILED", error_msg="No FCM Token")
                continue

            diff_minutes = (now_utc - earliest_time).total_seconds() / 60

            if 10 <= diff_minutes <= 11:
                log_title = "💊 [재알림] 약 드셨나요?"
                log_body = f"{user.username}님, [{regihistory.label}] 복용 시간 10분이 지났습니다. 잊지 말고 챙겨드세요!"
                log_type = "REMINDER_10MIN"
            elif 20 <= diff_minutes <= 21:
                log_title = "💊 [2차 알림] 약 복용 잊으셨나요?"
                log_body = f"{user.username}님, [{regihistory.label}] 복용 시간 20분이 지났습니다. 건강을 위해 지금 복용해주세요."
                log_type = "REMINDER_20MIN"
            else:
                continue

            plan_ids = [str(p.id) for p in plans]

            message = messaging.Message(
                token=token,
                data={
                    "type": "ALARM",
                    "title": log_title,
                    "body": log_body,
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

            # ✅ [성공 로그 저장]
            _save_notification_log(
                regihistory, log_type, "SUCCESS",
                title=log_title, body=log_body,
                metadata={"diff_minutes": diff_minutes}
            )

            logger.info(f"[REMINDER] {log_type} 전송 완료 → User: {user.username}")

        except Exception as e:
            # ❌ [실패 로그 저장]
            _save_notification_log(
                regihistory, log_type, "FAILED",
                title=log_title, body=log_body,
                error_msg=str(e)
            )
            logger.error(f"[REMINDER] 전송 실패 error={e}")

    return f"재알림 완료: {success_count}건"


@shared_task
def delete_plan_async(plan_id):
    try:
        plan = Plan.objects.get(id=plan_id)
        plan.delete()
        return f"Plan {plan_id} deleted successfully"
    except Plan.DoesNotExist:
        return f"Plan {plan_id} not found"