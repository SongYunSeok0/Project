# smart_med/users/tasks.py (경로는 본인 앱 위치에 맞게)
from celery import shared_task
from . import services  # 기존에 작성하신 services 파일을 가져옵니다.

@shared_task
def send_email_task(email, name):
    # 여기서 기존 서비스 로직을 호출합니다.
    # Celery가 백그라운드에서 이 함수를 대신 실행해줍니다.
    try:
        services.send_verification_email(email, name)
        return "Email sent successfully"
    except Exception as e:
        return f"Email sending failed: {str(e)}"