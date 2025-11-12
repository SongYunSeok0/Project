# iot/views.py
from django.db import transaction
from django.utils.dateparse import parse_datetime
from django.utils import timezone
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from .models import Device, SensorData
from health.models import HeartRate

def _to_bool(v):
    if isinstance(v, bool):
        return v
    s = str(v).strip().lower()
    return s in {"1", "true", "t", "yes", "y", "on"}

@api_view(["POST"])
@permission_classes([AllowAny])
def ingest(request):
    p = request.data

    # 1) 키 호환 매핑
    is_opened = _to_bool(p.get("is_opened", p.get("isOpened", False)))
    is_time   = _to_bool(p.get("is_time",   p.get("isTime",   False)))
    bpm       = p.get("bpm", p.get("Bpm"))
    ts_str    = p.get("collected_at", p.get("timestamp"))

    # 2) 시각 처리: ISO8601 우선, 실패 시 now()
    ts = parse_datetime(ts_str) if isinstance(ts_str, str) else None
    ts = ts or timezone.now()

    # 3) 디바이스 식별: id 또는 uuid
    device = None
    device_id = p.get("device_id")
    device_uuid = p.get("device_uuid")
    try:
        if device_id:
            device = Device.objects.select_related("user").get(id=device_id)
        elif device_uuid:
            device = Device.objects.select_related("user").get(device_uuid=device_uuid)
        else:
            return Response({"error": "device_id 또는 device_uuid 필요"}, status=status.HTTP_400_BAD_REQUEST)
    except Device.DoesNotExist:
        return Response({"error": "디바이스를 찾을 수 없음"}, status=status.HTTP_404_NOT_FOUND)

    # 4) 사용자 결정: 명시 user_id 없으면 디바이스 소유자
    user_id = p.get("user_id") or device.user_id

    # 5) 저장 (원자적)
    with transaction.atomic():
        SensorData.objects.create(
            device=device,
            user_id=user_id,
            is_opened=is_opened,
            is_time=is_time,
            collected_at=ts,
        )

        # 심박 별도 테이블 저장(유효 범위 검증)
        if bpm is not None:
            try:
                ibpm = int(bpm)
                if 20 <= ibpm <= 240:
                    HeartRate.objects.create(user_id=user_id, bpm=ibpm, collected_at=ts)
            except (TypeError, ValueError):
                pass

    # 6) 디바이스 건강상태 갱신
    Device.objects.filter(id=device.id).update(last_connected_at=timezone.now())

    return Response({"ok": True}, status=status.HTTP_200_OK)
