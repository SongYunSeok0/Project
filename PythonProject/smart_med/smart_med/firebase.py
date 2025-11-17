# smart_med/firebase.py
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
