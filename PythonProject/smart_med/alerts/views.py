from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from medications.models import Medication
from medications.services import process_sensor
from .fcm_service import send_fcm_notification
from users.models import User

from .models import SensorData


# @api_view(['POST'])
# @permission_classes([IsAuthenticated])
# def sensor_payload(request):
#     """
#     IoT에서 전송된 센서 데이터 처리 API + 보호자 알림 전송
#     예시 body:
#     {
#       "user_id": 1,
#       "medication_id": 5,
#       "weight": 0.35
#     }
#     """
#     try:
#         data = request.data
#         user_id = data.get("user_id")
#         med_id = data.get("medication_id")
#         weight = float(data.get("weight", 0))
#
#         # 복용 상태 판별 및 DB 반영
#         result = process_sensor(user_id, med_id, weight)
#
#         # 약 정보 업데이트 (마지막 체크 시간 기록)
#         med = Medication.objects.get(id=med_id, user_id=user_id)
#         med.last_checked = timezone.now()
#         med.save()
#
#         # 보호자에게 FCM 푸시 전송
#         user = User.objects.get(id=user_id)
#         protector = user.protectors.first()  # 보호자 1명만 예시로 전송
#         if protector and getattr(protector, "fcm_token", None):
#             send_fcm_notification(
#                 token=protector.fcm_token,
#                 title="복약 알림",
#                 body=result.get("message", "복용 상태 업데이트")
#             )
#
#         return Response({
#             "message": "센서 데이터 처리 및 알림 전송 완료",
#             "user_id": user_id,
#             "medication_id": med_id,
#             "weight": weight,
#             "status": result.get("status")
#         }, status=status.HTTP_200_OK)
#
#     except Medication.DoesNotExist:
#         return Response(
#             {"error": "해당 약 정보를 찾을 수 없습니다."},
#             status=status.HTTP_404_NOT_FOUND
#         )
#     except Exception as e:
#         return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)
#
#

@api_view(['POST'])
# @permission_classes([IsAuthenticated])
@permission_classes([AllowAny])
def sensor_payload(request):
    data = request.data
    sensor_value = data.get("Sensor")
    heart_rate = data.get("heart_rate")
    is_opened = data.get("isOpened")

    # 로그 확인용
    print(f"센서 수신: Sensor={sensor_value}, heart_rate={heart_rate}, isOpened={is_opened}")

    # DB 저장 예시
    SensorData.objects.create(
        sensor_value=sensor_value,
        heart_rate=heart_rate,
        is_opened=is_opened
    )

    return Response({"message": "센서 데이터 수신 완료 ✅"}, status=200)


@api_view(['GET'])
@permission_classes([AllowAny])
def get_command(request):
    """
    IoT 장치가 주기적으로 명령을 가져가는 API
    예시 응답: {"time": true, "open": false}
    """
    command = {
        "time": True  # 시간 알림 신호 예시
         }
    print("✅ ESP32 명령 요청 수신:", command)
    return Response(command)