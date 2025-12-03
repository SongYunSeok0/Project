import os
from django.conf import settings

import firebase_admin
from firebase_admin import credentials, messaging

# 앱 초기화
if not firebase_admin._apps:
    # settings 에서 바로 읽기
    cred_path = getattr(settings, "FIREBASE_CREDENTIAL_PATH", None)
    if not cred_path:
        cred_path = os.path.join(settings.BASE_DIR, "smart_med_firebase_admin.json")

    print("[FCM] use credential:", cred_path)

    if not os.path.exists(cred_path):
        raise FileNotFoundError(f"[FCM] credential file not found: {cred_path}")

    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred)
    print("[FCM] firebase_admin initialized")


def send_fcm_to_token(token: str, title: str, body: str, data: dict | None = None) -> str:
    print("[FCM] send_fcm_to_token called")
    print("[FCM] token =", token)
    print("[FCM] title =", title, "body =", body)

    # 데이터 페이로드 정리 (모든 값을 문자열로 변환)
    payload_data = {
        "title": title,
        "body": body,
        **{k: str(v) for k, v in (data or {}).items()},
    }

    msg = messaging.Message(
        token=token,
        # 1. 기본 알림 (화면 켜짐/꺼짐 공통)
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        # 2. 데이터 메시지
        data=payload_data,

        # 3. [핵심] 안드로이드 전용 설정 (앱 꺼짐/Doze 모드 깨우기용)
        android=messaging.AndroidConfig(
            priority='high',  # 'high'로 설정해야 절전 모드에서도 즉시 알림이 옴
            notification=messaging.AndroidNotification(
                channel_id='medication_alarm_channel',  # 안드로이드 앱 소스에 설정된 채널 ID와 일치해야 함
                click_action='FLUTTER_NOTIFICATION_CLICK',  # 앱 아이콘 클릭 시 동작 (Flutter 기준 기본값, Native도 무방)
                sound='default'  # 소리 설정
            ),
        ),
    )

    print("[FCM] message =", msg)

    try:
        res = messaging.send(msg)
        print("[FCM] sent =", res)
        return res
    except Exception as e:
        print("[FCM] Error sending message:", e)
        # 필요하다면 에러를 다시 raise 하거나 None 반환
        return str(e)