from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from .models import Faq, FaqComment
from .serializers import FaqSerializer, FaqCommentSerializer

class FaqViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    serializer_class = FaqSerializer
    
    def get_queryset(self):
        # 일반 사용자: 본인 문의사항만
        # 스태프: 모든 문의사항
        if self.request.user.is_staff:
            return Faq.objects.all()
        return Faq.objects.filter(user=self.request.user)
    
    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
    
    @action(detail=False, methods=['get'], url_path='all')
    def get_all(self, request):
        """스태프 전용: 모든 문의사항 조회"""
        if not request.user.is_staff:
            return Response(
                {'detail': '권한이 없습니다.'},
                status=status.HTTP_403_FORBIDDEN
            )
        
        faqs = Faq.objects.all()
        serializer = self.get_serializer(faqs, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['get'], url_path='comments')
    def get_comments(self, request, pk=None):
        """특정 FAQ의 댓글 목록 조회"""
        faq = self.get_object()
        comments = faq.comments.all()
        serializer = FaqCommentSerializer(comments, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'], url_path='comments')
    def add_comment(self, request, pk=None):
        """FAQ에 댓글 추가"""
        faq = self.get_object()
        
        # 스태프만 댓글 작성 가능
        if not request.user.is_staff:
            return Response(
                {'detail': '댓글 작성 권한이 없습니다.'},
                status=status.HTTP_403_FORBIDDEN
            )
        
        serializer = FaqCommentSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save(user=request.user, faq=faq)
            
            # 댓글이 달리면 is_answered = True로 변경
            faq.is_answered = True
            faq.save()
            
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)