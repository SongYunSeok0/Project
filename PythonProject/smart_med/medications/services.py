# medications/services.py

import datetime
from django.utils import timezone
from django.shortcuts import get_object_or_404
from .models import RegiHistory, Plan
from smart_med.utils.time_utils import from_ms


def create_single_plan(user, data):
    """
    단건 일정 생성 로직
    """
    regi_id = data.get("regihistoryId")
    regi = get_object_or_404(RegiHistory, id=regi_id, user=user)

    dt = from_ms(data.get("takenAt"))

    # 영양제인 경우 약 이름 비우기 (기존 로직 유지)
    med_name = data.get("medName")
    if regi.regi_type == "supplement":
        med_name = ""

    plan = Plan.objects.create(
        regihistory=regi,
        med_name=med_name,
        taken_at=dt,
        ex_taken_at=dt,  # 예정 시간 초기화
        meal_time=data.get("mealTime") or "before",
        note=data.get("note"),
        taken=from_ms(data.get("taken")),
        use_alarm=data.get("useAlarm", True),
    )
    return plan


def create_smart_schedule(user, data):
    """
    스마트 일정 일괄 생성 로직 (복잡한 루프 처리)
    """
    rid = data.get("regihistoryId")
    regi = get_object_or_404(RegiHistory, id=rid, user=user)

    start_date_str = data.get("startDate")  # "YYYY-MM-DD"
    duration = int(data.get("duration", 1))
    times = data.get("times", [])  # ["08:00", "13:00", ...]
    med_name = data.get("medName", "")

    # 날짜 파싱
    try:
        current_date = datetime.datetime.strptime(start_date_str, "%Y-%m-%d").date()
    except (ValueError, TypeError):
        current_date = timezone.localdate()

    now = timezone.now()
    created_plans = []
    total_target = duration * len(times)
    count = 0

    # 목표 개수만큼 생성할 때까지 날짜를 하루씩 늘려가며 반복
    while count < total_target:
        for t in sorted(times):
            if count >= total_target:
                break

            hour, minute = map(int, t.split(":"))
            dt = datetime.datetime.combine(current_date, datetime.time(hour, minute))

            # Timezone 처리
            if timezone.is_naive(dt):
                dt = timezone.make_aware(dt, timezone.get_current_timezone())

            # 이미 지난 시간은 스킵 (선택 사항이지만 기존 로직 유지)
            if dt <= now:
                continue

            p = Plan.objects.create(
                regihistory=regi,
                med_name=med_name,
                taken_at=dt,
                ex_taken_at=dt,
                meal_time="after",  # 스마트 생성은 기본값 after
                use_alarm=True,
            )
            created_plans.append(p)
            count += 1

        # 하루 증가
        current_date += datetime.timedelta(days=1)

    # 동기화 타임스탬프 업데이트 (한 번에 쿼리)
    if created_plans:
        sync_time = timezone.now()
        plan_ids = [p.id for p in created_plans]
        Plan.objects.filter(id__in=plan_ids).update(updated_at=sync_time)

        # 반환할 객체들에도 업데이트 반영
        for p in created_plans:
            p.updated_at = sync_time

    return created_plans


def update_plan_time(user, plan_id, data):
    """
    일정 시간 수정 및 관련 업데이트
    """
    plan = get_object_or_404(Plan, id=plan_id, regihistory__user=user)

    # 시간 업데이트
    if "takenAt" in data:
        raw = data["takenAt"]
        # int(ms) 또는 string(iso) 처리
        if isinstance(raw, (int, float)):
            new_dt = datetime.datetime.fromtimestamp(raw / 1000, tz=datetime.timezone.utc)
        else:
            from django.utils.dateparse import parse_datetime
            new_dt = parse_datetime(raw)

        plan.taken_at = new_dt

    if "medName" in data:
        plan.med_name = data["medName"]
    if "useAlarm" in data:
        plan.use_alarm = data["useAlarm"]

    plan.save()
    return plan


def mark_plan_as_taken(user, plan_id):
    """
    약 복용 완료 처리
    """
    plan = get_object_or_404(Plan, id=plan_id, regihistory__user=user)

    # 이미 복용했다면 패스 (또는 업데이트)
    if not plan.taken:
        plan.taken = timezone.now()
        plan.save()

    return plan


def snooze_plan(user, plan_id, minutes=10):
    """
    알림 미루기 (시간 연장)
    """
    plan = get_object_or_404(Plan, id=plan_id, regihistory__user=user)

    # 이미 복용했으면 미루기 불가
    if plan.taken:
        raise ValueError("이미 복용한 약입니다.")

    # 현재 시간 or 예정 시간 기준으로 연장
    base_time = plan.taken_at if plan.taken_at > timezone.now() else timezone.now()
    plan.taken_at = base_time + datetime.timedelta(minutes=minutes)

    # 알림 사용 강제 켜기
    plan.use_alarm = True
    plan.save()

    return plan