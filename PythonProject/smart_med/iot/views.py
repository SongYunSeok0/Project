# iot/views.py
import secrets
from pathlib import Path
from django.db import transaction
from django.http import FileResponse
from django.utils import timezone
from django.utils.dateparse import parse_datetime
from rest_framework import permissions
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Device, SensorData, IntakeStatus
from health.models import HeartRate
from smart_med.utils.make_qr import create_qr


from .docs import (
    ingest_docs,
    command_docs,
    qr_docs,
    register_device_docs
)


# ==========================================
# Ingest API
# ==========================================
@ingest_docs
@api_view(["POST"])
@permission_classes([permissions.AllowAny])
def ingest(request):
    p = request.data

    def _bool(v):
        if isinstance(v, bool):
            return v
        return str(v).lower() in {"1", "true", "yes", "y"}

    is_opened = _bool(p.get("is_opened", p.get("isOpened", False)))
    is_time = _bool(p.get("is_time", p.get("isTime", False)))
    bpm = p.get("bpm") or p.get("Bpm")

    ts_str = p.get("timestamp") or p.get("collected_at")
    ts = parse_datetime(ts_str) if isinstance(ts_str, str) else None
    ts = ts or timezone.now()

    # device lookup
    device_uuid = p.get("device_uuid")
    device_id = p.get("device_id")

    try:
        if device_id:
            device = Device.objects.get(id=device_id)
        else:
            device = Device.objects.get(device_uuid=device_uuid)
    except Device.DoesNotExist:
        return Response({"error": "device not found"}, status=404)

    user_id = p.get("user_id") or device.user_id

    # 복약 상태 판정
    if is_time and is_opened:
        status_code = IntakeStatus.TAKEN
    elif not is_time and is_opened:
        status_code = IntakeStatus.WRONG
    elif is_time and not is_opened:
        status_code = IntakeStatus.MISSED
    else:
        status_code = IntakeStatus.NONE

    # 저장
    with transaction.atomic():
        SensorData.objects.create(
            device=device,
            user_id=user_id,
            is_opened=is_opened,
            is_time=is_time,
            collected_at=ts,
            status=status_code,
        )
        if bpm:
            try:
                ibpm = int(bpm)
                if 20 <= ibpm <= 240:
                    HeartRate.objects.create(
                        user_id=user_id,
                        bpm=ibpm,
                        collected_at=ts
                    )
            except:
                pass

    device.last_connected_at = timezone.now()
    device.save(update_fields=["last_connected_at"])

    return Response({
        "ok": True,
        "status": status_code,
        "raw": {"is_opened": is_opened, "is_time": is_time, "bpm": bpm},
        "timestamp": ts,
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

        filename = create_qr(device.device_uuid, device.device_token)
        filepath = Path(filename)

        if not filepath.exists():
            return Response({"error": "QR not found"}, status=404)

        return FileResponse(open(filepath, "rb"), content_type="image/png")


# ==========================================
# Register Device
# ==========================================
@register_device_docs
class RegisterDeviceView(APIView):
    permission_classes = [permissions.IsAuthenticated]

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

        # token 검증
        if device.device_token != token:
            return Response({"error": "invalid token"}, status=401)

        # user 연결
        device.user = request.user

        # device_name 있으면 업데이트
        if device_name:
            device.device_name = device_name

        device.save(update_fields=["user", "device_name"])

        return Response({
            "detail": "device connected",
            "device_uuid": device.device_uuid,
            "device_name": device.device_name,
            "user_id": request.user.id
        })


