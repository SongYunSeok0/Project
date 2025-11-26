import os
from django.conf import settings

import firebase_admin
from firebase_admin import credentials, messaging


def initialize_firebase():
    """Firebase 초기화를 지연 로딩 방식으로 실행."""
    if firebase_admin._apps:
        # 이미 초기화됨
        return firebase_admin.get_app()

    # 이제서야 settings 읽기
    cred_path = getattr(settings, "FIREBASE_CREDENTIAL_PATH", None)
    if not cred_path:
        cred_path = os.path.join(settings.BASE_DIR, "smart_med_firebase_admin.json")

    print("[FCM] use credential:", cred_path)

    if not os.path.exists(cred_path):
        raise FileNotFoundError(f"[FCM] credential file not found: {cred_path}")

    cred = credentials.Certificate(cred_path)
    app = firebase_admin.initialize_app(cred)
    print("[FCM] firebase_admin initialized")
    return app


def send_fcm_to_token(token: str, title: str, body: str, data: dict | None = None) -> str:
    """FCM 메시지 전송 함수"""
    # 여기서 Firebase 초기화 상태를 확인하고 필요시 초기화
    initialize_firebase()

    print("[FCM] send_fcm_to_token called")
    print("[FCM] token =", token)
    print("[FCM] title =", title, "body =", body)

    msg = messaging.Message(
        token=token,
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        data={
            "title": title,
            "body": body,
            **{k: str(v) for k, v in (data or {}).items()},
        },
    )

    print("[FCM] message =", msg)
    res = messaging.send(msg)
    print("[FCM] sent =", res)
    return res
