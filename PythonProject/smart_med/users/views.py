# users/views.py
import traceback

from django.db import IntegrityError
from django.contrib.auth import get_user_model
from rest_framework.throttling import ScopedRateThrottle
from rest_framework.permissions import AllowAny, IsAuthenticated, BasePermission
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import generics
from .tasks import send_email_task
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView
from rest_framework.decorators import api_view, permission_classes

# [NEW] 서비스 모듈 임포트
from . import services
from .serializers import (
    UserCreateSerializer,
    UserUpdateSerializer,
    UserSerializer,
)
# (docs 임포트는 그대로 유지...)
from .docs import (
    jwt_login_docs, social_login_docs,
    me_get_docs, me_patch_docs,
    register_fcm_docs, send_email_code_docs,
    verify_email_code_docs, signup_docs, withdraw_docs,
)

User = get_user_model()


# ============================================================
# 권한 설정
# ============================================================
class IsStaffUser(BasePermission):
    def has_permission(self, request, view):
        return bool(request.user and request.user.is_authenticated and request.user.is_staff)


# ============================================================
# JWT Login
# ============================================================
@jwt_login_docs
class CustomTokenObtainPairView(TokenObtainPairView):
    def post(self, request, *args, **kwargs):
        response = super().post(request, *args, **kwargs)

        if response.status_code == 200:
            login_id = request.data.get("username") or request.data.get("email") or request.data.get("id")

            # [Refactoring] 서비스 호출로 대체
            user = services.get_user_by_login_id(login_id)
            if user:
                services.set_login_cache(user.id)
                print(f"[Login] Set cache for user {user.id}")

        return response


# ============================================================
# SOCIAL LOGIN
# ============================================================
@social_login_docs
class SocialLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        provider = request.data.get("provider")
        social_id = request.data.get("socialId")

        if not provider or not social_id:
            return Response({"detail": "provider/socialId 필요"}, status=400)

        # [Refactoring] 복잡한 생성 로직을 서비스로 위임
        user, is_created = services.social_login_get_or_create(provider, social_id)

        # 토큰 발급 (뷰의 역할: 응답 포맷팅)
        refresh = RefreshToken.for_user(user)

        if not is_created:
            # 기존 유저라면 로그인 캐시 설정
            services.set_login_cache(user.id)

        return Response({
            "access": str(refresh.access_token),
            "refresh": str(refresh),
            "needAdditionalInfo": is_created,  # 신규 유저면 True
        })


# ============================================================
# MeView (조회/수정은 간단해서 View 유지)
# ============================================================
@me_get_docs
@me_patch_docs
class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        return Response(UserSerializer(request.user).data)

    def patch(self, request):
        ser = UserUpdateSerializer(request.user, data=request.data, partial=True)
        ser.is_valid(raise_exception=True)
        ser.save()
        return Response(UserSerializer(request.user).data)


# ============================================================
# FCM Token
# ============================================================
@register_fcm_docs
class RegisterFcmTokenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        token = request.data.get("fcm_token")
        if not token:
            return Response({"detail": "fcm_token 누락"}, status=400)

        # [Refactoring] 서비스 호출
        services.register_fcm_and_notify(request.user, token)

        return Response({"detail": "ok"})


# ============================================================
# 이메일 관련 (중복체크 / 코드전송 / 검증)
# ============================================================
@api_view(['POST'])
@permission_classes([AllowAny])
def check_email_duplicate(request):
    email = request.data.get("email")
    if not email:
        return Response({"detail": "email 필요"}, status=400)
    exists = User.objects.filter(email=email).exists()
    return Response({"exists": exists})


@send_email_code_docs
class SendEmailCodeView(APIView):
    permission_classes = [AllowAny]
    throttle_classes = [ScopedRateThrottle]
    throttle_scope = 'sms_send'

    def post(self, request):
        email = request.data.get("email")
        name = request.data.get("name")

        if not email:
            return Response({"detail": "email 필요"}, status=400)

        # [수정 전] 직접 실행 (느림)
        # services.send_verification_email(email, name)

        # [수정 후] Celery에게 토스! (엄청 빠름) 🚀
        # .delay()를 붙이면 "나중에 해" 하고 바로 넘어갑니다.
        print("🚀 Celery에게 작업 넘기기 직전!")  # 로그 확인용
        send_email_task.delay(email, name)
        print("✅ Celery에게 작업 넘기기 완료! (여기까지 순식간이어야 함)")

        return Response({"detail": "인증코드가 발송되었습니다."})


@verify_email_code_docs
class VerifyEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")
        code = request.data.get("code")

        # [Refactoring] 서비스 호출
        success, message = services.verify_email_code(email, code)

        if success:
            return Response({"detail": message})
        else:
            return Response({"detail": message}, status=400)


# ============================================================
# Signup & Withdrawal
# ============================================================
@signup_docs
class SignupView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")

        # [Refactoring] 인증 여부 확인
        if not services.check_email_verified(email):
            return Response({"detail": "이메일 인증이 필요합니다."}, status=400)

        serializer = UserCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        try:
            user = serializer.save()
            # [Refactoring] 가입 완료 후 캐시 정리
            services.clear_email_cache(email)

            return Response({"message": "회원가입 성공", "user_id": user.id}, status=201)

        except IntegrityError as e:
            if "email" in str(e).lower():
                return Response({"detail": "이미 존재하는 이메일입니다."}, status=400)
            if "phone" in str(e).lower():
                return Response({"detail": "이미 존재하는 전화번호입니다."}, status=400)
            return Response({"detail": "회원가입 오류"}, status=400)


@withdraw_docs
class WithdrawalView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        request.user.delete()
        return Response({"message": "회원 탈퇴 완료"})


# ============================================================
# Admin Views
# ============================================================
class UserListView(generics.ListAPIView):
    queryset = User.objects.all().order_by('-created_at')
    serializer_class = UserSerializer
    permission_classes = [IsStaffUser]


class UserDetailView(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [IsStaffUser]
    lookup_field = 'id'