import secrets
import traceback
from django.core.mail import send_mail
from django.core.cache import cache
from django.conf import settings
from django.db import IntegrityError
from django.contrib.auth import authenticate, get_user_model

from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView

from .serializers import UserCreateSerializer, UserUpdateSerializer, UserSerializer
from notifications.services import send_fcm_to_token

# Swagger docs import
from .docs import (
    jwt_login_docs, social_login_docs,
    me_get_docs, me_patch_docs,
    register_fcm_docs, send_email_code_docs,
    verify_email_code_docs, signup_docs, withdraw_docs
)

User = get_user_model()


# ==========================================================
#  JWT LOGIN
# ==========================================================

@jwt_login_docs
class CustomTokenObtainPairView(TokenObtainPairView):
    def post(self, request, *args, **kwargs):
        response = super().post(request, *args, **kwargs)

        if response.status_code == 200:
            try:
                login_id = request.data.get("id") or request.data.get("email")
                if login_id:
                    user = User.objects.filter(username=login_id).first() or \
                           User.objects.filter(email=login_id).first()
                    if user:
                        cache.set(f"just_logged_in:{user.id}", True, timeout=60)
            except:
                pass

        return response


# ==========================================================
#  SOCIAL LOGIN
# ==========================================================

@social_login_docs
class SocialLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    @staticmethod
    def generate_unique_username(base):
        username = base
        count = 1
        while User.objects.filter(username=username).exists():
            username = f"{base}_{count}"
            count += 1
        return username

    def post(self, request):
        provider = request.data.get("provider")
        social_id = request.data.get("socialId")

        try:
            user = User.objects.get(provider=provider, social_id=social_id)

            refresh = RefreshToken.for_user(user)
            cache.set(f"just_logged_in:{user.id}", True, timeout=60)

            return Response({
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "needAdditionalInfo": False
            })

        except User.DoesNotExist:
            base_username = f"{provider}_{social_id}"
            username = self.generate_unique_username(base_username)

            user = User.objects.create_user(
                username=username,
                email=None,
                password=None,
                provider=provider,
                social_id=social_id,
            )

            refresh = RefreshToken.for_user(user)

            return Response({
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "needAdditionalInfo": True
            })


# ==========================================================
#  MeView
# ==========================================================

@me_get_docs
@me_patch_docs
class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        u = request.user
        return Response(UserSerializer(u).data)

    def patch(self, request):
        user = request.user
        serializer = UserUpdateSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(UserSerializer(user).data)


# ==========================================================
#  Register FCM
# ==========================================================

@register_fcm_docs
class RegisterFcmTokenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        token = request.data.get("fcm_token")
        if not token:
            return Response({"detail": "fcm_token 누락"}, status=400)

        user = request.user
        user.fcm_token = token
        user.save()

        key = f"just_logged_in:{user.id}"
        if cache.get(key):
            try:
                send_fcm_to_token(
                    token=token,
                    title="로그인 알림",
                    body=f"{user.username} 님이 로그인했습니다."
                )
            except:
                traceback.print_exc()

            cache.delete(key)

        return Response({"detail": "ok"})


# ==========================================================
# Send Email Code
# ==========================================================

@send_email_code_docs
class SendEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")
        if not email:
            return Response({"detail": "email 필요"}, status=400)

        code = secrets.randbelow(900000) + 100000
        cache.set(f"email_code:{email}", code, timeout=180)

        send_mail(
            "이메일 인증코드",
            f"인증코드: {code}\n3분 안에 입력해주세요.",
            settings.EMAIL_HOST_USER,
            [email],
        )

        return Response({"detail": "인증코드가 발송되었습니다."})


# ==========================================================
# Verify Email Code
# ==========================================================

@verify_email_code_docs
class VerifyEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")
        code = request.data.get("code")

        saved = cache.get(f"email_code:{email}")
        if saved is None:
            return Response({"detail": "코드 없음 또는 만료"}, status=400)

        if str(saved) != str(code):
            return Response({"detail": "코드 불일치"}, status=400)

        cache.set(f"email_verified:{email}", True, timeout=300)
        return Response({"detail": "인증 성공"})


# ==========================================================
# Signup
# ==========================================================

@signup_docs
class SignupView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")

        if not cache.get(f"email_verified:{email}"):
            return Response({"detail": "이메일 인증이 필요합니다."}, status=400)

        serializer = UserCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        try:
            user = serializer.save()
        except IntegrityError:
            return Response({"detail": "중복된 이메일 또는 전화번호"}, status=400)

        cache.delete(f"email_verified:{email}")
        cache.delete(f"email_code:{email}")

        return Response({"message": "회원가입 성공", "user_id": user.id}, status=201)


# ==========================================================
# Withdrawal
# ==========================================================

@withdraw_docs
class WithdrawalView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        request.user.delete()
        return Response({"message": "회원 탈퇴 완료"})
