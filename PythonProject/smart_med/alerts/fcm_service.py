import requests
import os

def send_fcm_notification(token, title, body):
    server_key = os.getenv("FCM_SERVER_KEY")
    url = "https://fcm.googleapis.com/fcm/send"
    headers = {
        "Authorization": f"key={server_key}",
        "Content-Type": "application/json",
    }
    payload = {
        "to": token,
        "notification": {"title": title, "body": body},
        "priority": "high",
    }

    response = requests.post(url, json=payload, headers=headers)
    return response.json()
