import os
from django.conf import settings

import firebase_admin
from firebase_admin import credentials, messaging


def initialize_firebase():
    """Firebase 초기화를 지연 로딩 방식으로 실행."""
    if firebase_admin._apps:
        # 이미 초기화됨
        return firebase_admin.get_app()

    # settings 에서 경로 읽기
    cred_path = getattr(settings, "FIREBASE_CREDENTIAL_PATH", None)
    if not cred_path:
        cred_path = os.path.join(settings.BASE_DIR, "smart_med_firebase_admin.json")

    # print("[FCM] use credential:", cred_path)

    if not os.path.exists(cred_path):
        raise FileNotFoundError(f"[FCM] credential file not found: {cred_path}")

    cred = credentials.Certificate(cred_path)
    app = firebase_admin.initialize_app(cred)
    print("[FCM] firebase_admin initialized")
    return app


def send_fcm_to_token(token: str, title: str, body: str, data: dict | None = None) -> str:
    """
    FCM 메시지 전송 함수
    - 화면 꺼짐: AlarmActivity 실행 (Data Message 처리)
    - 화면 켜짐: Heads-up Notification (상단 배너) 표시
    """
    # Firebase 초기화 상태 확인
    initialize_firebase()

    # print(f"[FCM] Sending to token prefix: {token[:10]}...")

    # 데이터 페이로드 정리
    # 1. 기본값으로 "type": "med_alarm" 설정
    # 2. 인자로 받은 data가 있다면 그 값으로 덮어씀 (예: "type": "ALARM")
    payload_data = {
        "type": "med_alarm",
        "title": title,
        "body": body,
        **{k: str(v) for k, v in (data or {}).items()},
    }

    msg = messaging.Message(
        token=token,

        # [중요] notification 필드를 비워둬야 안드로이드 앱의
        # onMessageReceived가 무조건 실행되어 커스텀 알람 처리가 가능합니다.
        # notification=None,

        # 데이터 메시지로만 전송
        data=payload_data,

        # 안드로이드 설정
        android=messaging.AndroidConfig(
            priority='high',  # 'high'여야 Doze 모드(절전)에서도 즉시 전송됨
            ttl=0,  # 즉시 전송 (지연 없음)

            # 아래 notification 설정은 Data Message 방식에서는 주석 처리가 맞습니다.
            # 앱(Android) 코드에서 직접 채널과 알림을 생성해야 하기 때문입니다.
        ),
    )

    try:
        res = messaging.send(msg)
        print(f"[FCM] Sent successfully. Type: {payload_data.get('type')}")
        return res
    except Exception as e:
        print(f"[FCM] Error sending message: {e}")
        return str(e)