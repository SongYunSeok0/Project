# iot/views.py
import secrets

from django.db import transaction
from django.http import FileResponse
from django.utils.dateparse import parse_datetime
from django.utils import timezone
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from pathlib import Path

from utils.make_qr import create_qr
from .models import Device, SensorData, IntakeStatus
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

    # 5) 복용 상태 판정 로직
    if is_time and is_opened:
        status_code = IntakeStatus.TAKEN
    elif not is_time and is_opened:
        status_code = IntakeStatus.WRONG
    elif is_time and not is_opened:
        status_code = IntakeStatus.MISSED
    else:
        status_code = IntakeStatus.NONE

    # 6) 저장
    with transaction.atomic():
        SensorData.objects.create(
            device=device,
            user_id=user_id,
            is_opened=is_opened,
            is_time=is_time,
            collected_at=ts,
            status=status_code
        )

        # 심박 별도 테이블 저장(유효 범위 검증)
        if bpm is not None:
            try:
                ibpm = int(bpm)
                if 20 <= ibpm <= 240:
                    HeartRate.objects.create(user_id=user_id, bpm=ibpm, collected_at=ts)
            except (TypeError, ValueError):
                pass

    # 7) 디바이스 건강상태 갱신
    Device.objects.filter(id=device.id).update(last_connected_at=timezone.now())

    print("📩 [IOT] sensor data:", request.data)

    return Response({
        "ok": True,
        "status": status_code,
        "raw": {
            "is_opened": is_opened,
            "is_time": is_time,
            "bpm": bpm
        },
        "timestamp": ts,
    })


class CommandView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        device_uuid = request.headers.get("X-DEVICE-UUID")
        device_token = request.headers.get("X-DEVICE-TOKEN")

        if not device_uuid or not device_token:
            return Response({"error": "missing device headers"}, status=401)

        try:
            device = Device.objects.get(device_uuid=device_uuid)
        except Device.DoesNotExist:
            return Response({"error": "invalid device"}, status=401)

        if device.device_token != device_token:
            return Response({"error": "invalid token"}, status=401)

        return Response({"time": True}, status=200)


class RegisterDeviceView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user

        device_uuid = secrets.token_hex(8)
        device_token = secrets.token_hex(32)

        device = Device.objects.create(
            user=user,
            device_uuid=device_uuid,
            device_token=device_token
        )

        return Response({
            "device_uuid": device_uuid,
            "device_token": device_token
        }, status=201)

class QRCodeView(APIView):
    permission_classes = [AllowAny]

    def get(self, request, device_uuid):
        # DB에서 해당 UUID 기기 찾기
        try:
            device = Device.objects.get(device_uuid=device_uuid)
        except Device.DoesNotExist:
            return Response({"error": "Device not found"}, status=404)

        # QR 생성 (uuid + token)
        filename = create_qr(device.device_uuid, device.device_token)
        filepath = Path(filename)

        if not filepath.exists():
            return Response({"error": "QR not found"}, status=404)

        return FileResponse(open(filepath, "rb"), content_type="image/png")
