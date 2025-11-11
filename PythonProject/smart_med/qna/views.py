from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import QnA
from .serializers import QnASerializer

@api_view(['GET'])
def get_answer(request):
    question = request.query_params.get('q', '')
    if not question:
        return Response({'error': '질문(q) 파라미터가 필요합니다.'}, status=400)

    # 가장 단순한 형태: 질문 포함 검색
    qna = QnA.objects.filter(question__icontains=question).first()

    if qna:
        serializer = QnASerializer(qna)
        return Response(serializer.data)
    else:
        return Response({'message': '관련 답변을 찾지 못했습니다.'})
