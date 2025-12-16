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
import datetime
from medications.models import Plan



from .models import Device, SensorData, IntakeStatus
from health.models import HeartRate
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

    # ===============================
    # 🔥 복용 타임 판단 (정해진 시간대인지)
    # ===============================
    now = timezone.now()
    threshold = datetime.timedelta(minutes=15)

    regi_list = device.regi_histories.all()
    plans = Plan.objects.filter(
        regihistory__in=regi_list,
        use_alarm=True
    )

    current_plan = None
    for p in plans:
        if p.taken_at and abs(p.taken_at - now) <= threshold:
            current_plan = p
            break

    # ===============================
    # 🔥 이번 타임에 이미 정상복용(TAKEN)한 적이 있는지
    # ===============================
    already_taken = False
    if current_plan:
        already_taken = SensorData.objects.filter(
            device=device,
            status=IntakeStatus.TAKEN,
            collected_at__gte=current_plan.taken_at - threshold,
            collected_at__lte=current_plan.taken_at + threshold,
        ).exists()

    # ===============================
    # 🔥 최종 status_code 결정 로직
    # ===============================
    if is_opened:
        if current_plan:
            if already_taken:
                status_code = IntakeStatus.WRONG  # 두 번째 열림 → 오복용
            else:
                if is_time:
                    status_code = IntakeStatus.TAKEN  # 첫 정상 복용
                else:
                    status_code = IntakeStatus.WRONG  # 시간 안 맞음 → 오복용
        else:
            status_code = IntakeStatus.WRONG  # 시간대 아님 → 무조건 오복용

    else:
        if is_time:
            status_code = IntakeStatus.MISSED  # 시간인데 안 열림
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

        now = timezone.now()
        threshold = datetime.timedelta(minutes=15)

        regi_list = device.regi_histories.all()
        plans = Plan.objects.filter(
            regihistory__in=regi_list,
            use_alarm=True,
            taken__isnull=True
        )

        time_signal = False

        for p in plans:
            if not p.taken_at:
                continue

            diff = abs(p.taken_at - now)
            if diff <= threshold:
                time_signal = True
                break

        return Response({"time": time_signal})



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


class CreateDeviceView(APIView):
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        # 1) uuid/token 자동 생성
        uuid = generate_device_uuid()
        token = generate_device_token()

        # 2) Device DB 생성
        device = Device.objects.create(
            device_uuid=uuid,
            device_token=token,
        )

        # 3) QR 코드 생성
        qr_path = create_qr(uuid, token)

        # 4) 접근 가능한 URL로 변환
        qr_url = f"/media/qr/{uuid}.png"

        return Response({
            "device_id": device.id,
            "device_uuid": uuid,
            "device_token": token,
            "qr_url": qr_url,
            "qr_file": qr_path,
        })


class MyDeviceListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        devices = Device.objects.filter(user=request.user).values("id", "device_name")
        return Response(list(devices))
