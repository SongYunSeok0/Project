# iot/views.py
import secrets
from pathlib import Path
from django.db import transaction
from django.http import FileResponse
from django.utils import timezone
from smart_med.utils.time_utils import to_ms, from_ms, parse_ts
from smart_med.utils.data_utils import to_bool
from rest_framework import permissions
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Device, SensorData, IntakeStatus
from health.models import HeartRate
from smart_med.utils.make_qr import create_qr

from .docs import ingest_docs, command_docs, qr_docs, register_device_docs


# ==========================================
# Ingest API
# ==========================================
@ingest_docs
@api_view(["POST"])
@permission_classes([permissions.AllowAny])
def ingest(request):
    p = request.data

    # --------------------------
    # Header 인증 (CommandView와 동일하게)
    # --------------------------
    uuid = request.headers.get("X-DEVICE-UUID")
    token = request.headers.get("X-DEVICE-TOKEN")

    if not uuid or not token:
        return Response({"error": "missing headers"}, status=401)

    try:
        device = Device.objects.get(device_uuid=uuid)
    except Device.DoesNotExist:
        return Response({"error": "invalid device"}, status=401)

    if device.device_token != token:
        return Response({"error": "invalid token"}, status=401)

    # --------------------------
    # Payload 파싱
    # --------------------------

    is_opened = to_bool(p.get("is_opened") or p.get("isOpened"))
    is_time = to_bool(p.get("is_time") or p.get("isTime"))
    bpm_raw = p.get("bpm") or p.get("Bpm")

    timestamp = parse_ts(
        p.get("timestamp") or p.get("collected_at")
    )

    # user 보정 (등록되지 않은 기기면 user_id = None)
    user_id = device.user_id

    # --------------------------
    # Status 판정
    # --------------------------
    if is_time and is_opened:
        status_code = IntakeStatus.TAKEN
    elif not is_time and is_opened:
        status_code = IntakeStatus.WRONG
    elif is_time and not is_opened:
        status_code = IntakeStatus.MISSED
    else:
        status_code = IntakeStatus.NONE

    # --------------------------
    # DB 저장
    # --------------------------
    with transaction.atomic():
        SensorData.objects.create(
            device=device,
            user_id=user_id,
            is_opened=is_opened,
            is_time=is_time,
            collected_at=timestamp,
            status=status_code,
        )

        # BPM 저장
        if bpm_raw is not None:
            try:
                bpm = int(bpm_raw)
                if 20 <= bpm <= 240:
                    HeartRate.objects.create(
                        user_id=user_id,
                        bpm=bpm,
                        collected_at=timestamp
                    )
            except:
                pass

    # 마지막 통신 시간 갱신
    device.last_connected_at = timezone.now()
    device.save(update_fields=["last_connected_at"])

    return Response({
        "ok": True,
        "status": status_code,
        "timestamp": timestamp,
        "raw": {
            "is_opened": is_opened,
            "is_time": is_time,
            "bpm": bpm_raw,
        }
    })


# ==========================================
# Command Polling
# ==========================================
@command_docs
class CommandView(APIView):
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        uuid = request.headers.get("X-DEVICE-UUID")
        token = request.headers.get("X-DEVICE-TOKEN")

        if not uuid or not token:
            return Response({"error": "missing headers"}, status=401)

        try:
            device = Device.objects.get(device_uuid=uuid)
        except Device.DoesNotExist:
            return Response({"error": "invalid device"}, status=401)

        if device.device_token != token:
            return Response({"error": "invalid token"}, status=401)

        return Response({"time": True})


# ==========================================
# QR Code
# ==========================================
@qr_docs
class QRCodeView(APIView):
    permission_classes = [permissions.AllowAny]

    def get(self, request, device_uuid):
        try:
            device = Device.objects.get(device_uuid=device_uuid)
        except Device.DoesNotExist:
            return Response({"error": "Device not found"}, status=404)

        filepath = Path(create_qr(device.device_uuid, device.device_token))

        if not filepath.exists():
            return Response({"error": "QR not found"}, status=404)

        return FileResponse(open(filepath, "rb"), content_type="image/png")


# ==========================================
# Register Device
# ==========================================
@register_device_docs
class RegisterDeviceView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        uuid = request.data.get("uuid")
        token = request.data.get("token")
        device_name = request.data.get("device_name")

        if not uuid or not token:
            return Response({"error": "uuid/token required"}, status=400)

        try:
            device = Device.objects.get(device_uuid=uuid)
        except Device.DoesNotExist:
            return Response({"error": "invalid device"}, status=404)

        if device.device_token != token:
            return Response({"error": "invalid token"}, status=401)

        # user 연결
        device.user = request.user
        if device_name:
            device.device_name = device_name

        device.save(update_fields=["user", "device_name"])

        return Response({
            "detail": "device connected",
            "device_uuid": device.device_uuid,
            "device_name": device.device_name,
            "user_id": request.user.id
        })
