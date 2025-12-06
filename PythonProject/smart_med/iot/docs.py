from drf_spectacular.utils import extend_schema, OpenApiExample, OpenApiParameter, OpenApiResponse

# ==========
# Ingest API
# ==========
ingest_docs = extend_schema(
    tags=["IoT"],
    summary="IoT 센서 데이터 수집",
    description="""
IoT 기기가 서버로 상태(is_opened, is_time, bpm) 및 데이터를 전송하는 엔드포인트.

- device_uuid 또는 device_id 필수
- 복용 상태 자동 판정
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
            }
        }
    },
    responses={
        200: OpenApiResponse(
            examples=[
                OpenApiExample(
                    "샘플 응답",
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

# ==========
# Command Polling API
# ==========
command_docs = extend_schema(
    tags=["IoT"],
    summary="IoT 명령 요청 (Polling)",
    description="디바이스가 서버로 명령을 요청할 때 호출되는 엔드포인트.",
    parameters=[
        OpenApiParameter("X-DEVICE-UUID", str, OpenApiParameter.HEADER, required=True),
        OpenApiParameter("X-DEVICE-TOKEN", str, OpenApiParameter.HEADER, required=True),
    ],
    responses={
        200: OpenApiResponse(description="정상 응답: {'time': true}"),
        401: OpenApiResponse(description="기기 인증 실패")
    }
)

# ==========
# QR Code API
# ==========
qr_docs = extend_schema(
    tags=["IoT"],
    summary="IoT 기기 QR 코드 조회",
    description="디바이스 UUID 기반으로 등록용 QR(PNG)을 반환합니다.",
    responses={
        200: OpenApiResponse(description="PNG 이미지"),
        404: OpenApiResponse(description="디바이스 없음 또는 QR 생성 실패")
    }
)

# ==========
# Register Device API
# ==========
register_device_docs = extend_schema(
    tags=["IoT"],
    summary="IoT 기기 등록",
    description="""이미 생성되어 QR 코드에 담겨 있는 **device_uuid** 및 **device_token**을 사용하여  
    해당 IoT 기기를 현재 로그인한 사용자 계정에 연결하는 API입니다.

    ### 📌 QR 기반 등록 흐름
    1. 서버는 사전에 device_uuid, device_token을 생성해 QR 코드에 포함시켜둔다.
    2. 사용자가 앱에서 QR을 스캔하면 uuid/token이 추출된다.
    3. 앱은 이 API(`/iot/device/register`)에 uuid/token을 전달한다.
    4. 검증이 통과하면 기기가 사용자 계정에 연결된다.""",
    responses={
        201: OpenApiResponse(
            examples=[
                OpenApiExample(
                    "등록 성공",
                    value={
                        "device_uuid": "fa21bd3a9c4e88ff",
                        "device_token": "0db23fa92bcff129ab45d912edf009aa"
                    }
                )
            ]
        )
    }
)
