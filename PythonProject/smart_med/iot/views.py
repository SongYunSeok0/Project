# iot/views.py

import secrets
from pathlib import Path
from django.db import transaction
from django.http import FileResponse
from django.utils import timezone
from rest_framework import permissions
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Device, SensorData, IntakeStatus
from health.models import HeartRate

from smart_med.utils.time_utils import to_ms, from_ms, parse_ts
from smart_med.utils.data_utils import to_bool
from smart_med.utils.make_qr import create_qr

from .docs import ingest_docs, command_docs, qr_docs, register_device_docs


# ---------------------------------------------------------
# 디바이스가 상태 센서 데이터를 서버로 업로드하는 ingest API
# ---------------------------------------------------------
@ingest_docs
@api_view(["POST"])
@permission_classes([permissions.AllowAny])
def ingest(request):
    p = request.data

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

    is_opened = to_bool(p.get("is_opened") or p.get("isOpened"))
    is_time = to_bool(p.get("is_time") or p.get("isTime"))
    bpm_raw = p.get("bpm") or p.get("Bpm")
    timestamp = parse_ts(p.get("timestamp") or p.get("collected_at"))
    user_id = device.user_id

    if is_time and is_opened:
        status_code = IntakeStatus.TAKEN
    elif not is_time and is_opened:
        status_code = IntakeStatus.WRONG
    elif is_time and not is_opened:
        status_code = IntakeStatus.MISSED
    else:
        status_code = IntakeStatus.NONE

    with transaction.atomic():
        SensorData.objects.create(
            device=device,
            user_id=user_id,
            is_opened=is_opened,
            is_time=is_time,
            collected_at=timestamp,
            status=status_code,
        )

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


# ---------------------------------------------------------
# IoT 기기가 명령을 가져가는 Command Polling API
# ---------------------------------------------------------
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


# ---------------------------------------------------------
# 특정 디바이스의 QR 코드 이미지를 반환하는 API
# ---------------------------------------------------------
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


# ---------------------------------------------------------
# 현재 로그인한 사용자 계정에 기기를 등록(연결)하는 API
# ---------------------------------------------------------
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


# ---------------------------------------------------------
# 현재 로그인한 사용자가 등록한 IoT 기기 목록을 반환하는 API
# ---------------------------------------------------------
class MyDeviceListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        devices = Device.objects.filter(user=request.user).values("id", "device_name")
        return Response(list(devices))
