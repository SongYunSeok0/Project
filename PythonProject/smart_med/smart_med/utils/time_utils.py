import datetime
from django.utils import timezone

def to_ms(dt):
    """datetime → ms"""
    if dt is None:
        return None
    if isinstance(dt, datetime.date) and not isinstance(dt, datetime.datetime):
        dt = datetime.datetime.combine(
            dt,
            datetime.time.min,
            tzinfo=timezone.get_current_timezone()
        )
    if timezone.is_naive(dt):
        dt = timezone.make_aware(dt, timezone.get_current_timezone())
    return int(dt.timestamp() * 1000)


def from_ms(ms):
    """ms → datetime"""
    if ms in (None, "", 0):
        return None

    return datetime.datetime.fromtimestamp(
        ms / 1000.0,
        tz=timezone.get_current_timezone(),
    )