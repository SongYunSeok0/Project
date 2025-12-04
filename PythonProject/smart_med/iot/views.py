import secrets
from pathlib import Path
from django.db import transaction
from django.http import FileResponse
from django.utils.dateparse import parse_datetime
from django.utils import timezone
from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from drf_spectacular.utils import (
    extend_schema,
    OpenApiExample,
    OpenApiParameter,
    OpenApiResponse,
)
from health.models import HeartRate, DailyStep
from iot.models import Device, SensorData, IntakeStatus
from health.serializers import HeartRateSerializer, DailyStepSerializer
from utils.make_qr import create_qr

@extend_schema(
    tags=["Health - Heart Rate"],
    summary="심박수 기록 조회",
    description="로그인한 사용자 자신의 심박수 기록 전체를 최신순으로 조회합니다.",
    responses={200: HeartRateSerializer(many=True)},
)
class HeartRateViewSet(viewsets.ModelViewSet):
    queryset = HeartRate.objects.all()
    serializer_class = HeartRateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return HeartRate.objects.filter(user=self.request.user).order_by(
            "-collected_at", "-id"
        )

    @extend_schema(
        tags=["Health - Heart Rate"],
        summary="최신 심박수 조회",
        description="""가장 최근에 센싱된 심박수(bpm)를 반환합니다.
        데이터가 없으면 bpm=None 를 반환합니다.""",
        responses={
            200: OpenApiResponse(
                examples=[
                    OpenApiExample("데이터 있음", value={"bpm": 75, "collected_at": "2025-12-03T10:20:00Z"}),
                    OpenApiExample("데이터 없음", value={"bpm": None, "collected_at": None}),
                ]
            )
        },
    )
    @action(detail=False, methods=["get"])
    def latest(self, request):
        obj = self.get_queryset().order_by("-collected_at", "-id").first()
        if not obj:
            return Response({"bpm": None, "collected_at": None})
        return Response({"bpm": obj.bpm, "collected_at": obj.collected_at})

    @extend_schema(
        tags=["Health - Heart Rate"],
        summary="심박수 기록 생성",
        description="새로운 심박수 데이터를 저장합니다.",
        request=HeartRateSerializer,
        responses={201: HeartRateSerializer},
    )
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


@extend_schema(
    tags=["Health - Daily Step"],
    summary="일별 걸음수 목록 조회",
    description="로그인한 사용자 자신의 일별 걸음수 데이터를 조회합니다.",
    responses={200: DailyStepSerializer(many=True)},
)
class DailyStepViewSet(viewsets.ModelViewSet):
    queryset = DailyStep.objects.all()
    serializer_class = DailyStepSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return DailyStep.objects.filter(user=self.request.user)

    @extend_schema(
        tags=["Health - Daily Step"],
        summary="일별 걸음수 생성 또는 업데이트",
        description="""
같은 날짜(date)에 기록이 이미 있다면 **steps만 업데이트**하고,  
없다면 **새로운 데이터가 생성**됩니다.
        """,
        request=DailyStepSerializer,
        responses={
            200: DailyStepSerializer,
            201: DailyStepSerializer,
        },
    )
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
@extend_schema(
    tags=["IoT"],
    summary="IoT 센서 데이터 수집",
    description="""
IoT 기기(ESP32 / Raspberry Pi 등)에서 복약상자 이벤트 & 센서데이터를 서버로 전송할 때 사용하는 엔드포인트입니다.

### 수신 파라미터
- **device_uuid** 또는 **device_id** 필수  
- is_opened: 상자가 열림 여부  
- is_time: 복약 시간인지 여부  
- bpm: 심박 센서 값  
- timestamp / collected_at: ISO8601 (없으면 자동 now())

### 복용 상태 판정
- is_time=True & is_opened=True → TAKEN  
- is_time=False & is_opened=True → WRONG  
- is_time=True & is_opened=False → MISSED  
- 둘 다 False → NONE
""",
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "device_uuid": {"type": "string"},
                "is_opened": {"type": "boolean"},
                "is_time": {"type": "boolean"},
                "bpm": {"type": "integer"},
                "timestamp": {"type": "string", "format": "date-time"}
            },
        }
    },
    responses={
        200: OpenApiResponse(
            examples=[
                OpenApiExample(
                    "예시 응답",
                    value={
                        "ok": True,
                        "status": "TAKEN",
                        "raw": {"is_opened": True, "is_time": True, "bpm": 78},
                        "timestamp": "2025-12-03T10:11:22Z",
                    }
                )
            ]
        )
    }
)


@api_view(["POST"])
@permission_classes([AllowAny])
def ingest(request):
    p = request.data

    def _to_bool(v):
        if isinstance(v, bool): return v
        return str(v).lower() in {"1", "true", "yes", "y"}

    # 1) 데이터 파싱
    is_opened = _to_bool(p.get("is_opened", p.get("isOpened", False)))
    is_time = _to_bool(p.get("is_time", p.get("isTime", False)))
    bpm = p.get("bpm", p.get("Bpm"))
    ts_str = p.get("timestamp", p.get("collected_at"))
    ts = parse_datetime(ts_str) if isinstance(ts_str, str) else None
    ts = ts or timezone.now()

    # 2) 디바이스 조회
    device_uuid = p.get("device_uuid")
    device_id = p.get("device_id")

    try:
        if device_id:
            device = Device.objects.get(id=device_id)
        elif device_uuid:
            device = Device.objects.get(device_uuid=device_uuid)
        else:
            return Response({"error": "device_id 또는 device_uuid 필요"}, status=400)
    except Device.DoesNotExist:
        return Response({"error": "디바이스 없음"}, status=404)

    user_id = p.get("user_id") or device.user_id

    # 3) 상태 계산
    if is_time and is_opened:
        status_code = IntakeStatus.TAKEN
    elif not is_time and is_opened:
        status_code = IntakeStatus.WRONG
    elif is_time and not is_opened:
        status_code = IntakeStatus.MISSED
    else:
        status_code = IntakeStatus.NONE

    # 4) 저장
    with transaction.atomic():
        SensorData.objects.create(
            device=device,
            user_id=user_id,
            is_opened=is_opened,
            is_time=is_time,
            collected_at=ts,
            status=status_code
        )
        if bpm:
            try:
                ibpm = int(bpm)
                if 20 <= ibpm <= 240:
                    HeartRate.objects.create(user_id=user_id, bpm=ibpm, collected_at=ts)
            except:
                pass

    device.last_connected_at = timezone.now()
    device.save(update_fields=["last_connected_at"])

    return Response({
        "ok": True,
        "status": status_code,
        "raw": {"is_opened": is_opened, "is_time": is_time, "bpm": bpm},
        "timestamp": ts
    })


@extend_schema(
    tags=["IoT"],
    summary="IoT 명령 요청 (기기 Polling)",
    description="""
디바이스가 서버로 명령을 요청할 때 호출되는 API입니다.  
(예: 복약 시간 알림을 받을 때)

### 필요 헤더
- X-DEVICE-UUID  
- X-DEVICE-TOKEN
""",
    parameters=[
        OpenApiParameter("X-DEVICE-UUID", type=str, location=OpenApiParameter.HEADER, required=True),
        OpenApiParameter("X-DEVICE-TOKEN", type=str, location=OpenApiParameter.HEADER, required=True),
    ],
    responses={
        200: OpenApiResponse(description="정상 응답 예시: {'time': true}"),
        401: OpenApiResponse(description="기기 인증 실패")
    }
)
class CommandView(APIView):
    permission_classes = [AllowAny]

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

        return Response({"time": True}, status=200)


@extend_schema(
    tags=["IoT"],
    summary="IoT 기기 QR 코드 조회",
    description="디바이스 UUID 기반으로 등록용 QR 코드를 PNG 이미지로 반환합니다.",
    responses={
        200: OpenApiResponse(description="PNG 이미지 반환"),
        404: OpenApiResponse(description="디바이스 또는 QR 파일 없음")
    }
)
class QRCodeView(APIView):
    permission_classes = [AllowAny]

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


@extend_schema(
    tags=["IoT"],
    summary="IoT 기기 등록",
    description="""
로그인한 사용자를 기준으로 **새 IoT 기기(Device)** 를 생성하고  
기기 고유 UUID + TOKEN 을 발급합니다.

이 값은 기기 초기 설정 과정에서 사용됩니다.

### 반환 데이터
- device_uuid: 기기 고유 UUID
- device_token: 기기 인증 토큰(ESP32/RaspberryPi 저장)
""",
    responses={
        201: OpenApiResponse(
            examples=[
                OpenApiExample(
                    "예시 응답",
                    value={
                        "device_uuid": "fa21bd3a9c4e88ff",
                        "device_token": "0db23fa92bcff129ab45d912edf009aa"
                    }
                )
            ]
        ),
        401: OpenApiResponse(description="인증 필요")
    }
)
class RegisterDeviceView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        user = request.user

        device_uuid = secrets.token_hex(8)
        device_token = secrets.token_hex(32)

        device = Device.objects.create(
            user=user,
            device_uuid=device_uuid,
            device_token=device_token
        )

        return Response(
            {
                "device_uuid": device_uuid,
                "device_token": device_token
            },
            status=201
        )
