import datetime
from django.utils import timezone
from django.utils.dateparse import parse_datetime


def to_ms(dt):
    if dt is None:
        return None

    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(dt, datetime.time.min, tzinfo=timezone.get_current_timezone())

    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())

    return int(dt.timestamp() * 1000)


def from_ms(ms):
    if ms in (None, "", 0):
        return None

    return datetime.datetime.fromtimestamp(ms / 1000, tz=timezone.get_current_timezone())


def parse_ts(v):
    """
    지원:
    - 1700000000000 (ms)
    - 2025-12-04T12:00:00Z (ISO)
    - None → now()
    """
    if v is None:
        return timezone.now()

    # timestamp(ms)
    if isinstance(v, (int, float)):
        return timezone.make_aware(
            datetime.datetime.fromtimestamp(v / 1000.0)
        )

    # ISO 문자열
    if isinstance(v, str):
        dt = parse_datetime(v)
        if dt:
            if timezone.is_naive(dt):
                dt = timezone.make_aware(dt)
            return dt

    # fallback
    return timezone.now()
