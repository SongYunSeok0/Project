from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from .models import Prescription
from .utils import extract_text_from_image

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def upload_prescription(request):
    """
    처방전 이미지 업로드 → OCR 분석 → DB 저장
    """
    user = request.user
    image = request.FILES.get("image")

    if not image:
        return Response({"error": "이미지 파일이 필요합니다."}, status=400)

    # 임시 저장 및 OCR
    prescription = Prescription.objects.create(user=user, image=image)
    text = extract_text_from_image(prescription.image.path)
    prescription.extracted_text = text
    prescription.save()

    return Response({
        "message": "OCR 완료",
        "extracted_text": text
    }, status=status.HTTP_200_OK)
