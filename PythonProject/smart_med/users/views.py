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

from drf_spectacular.utils import (
    extend_schema,
    OpenApiResponse,
    OpenApiParameter,
    OpenApiExample
)

User = get_user_model()


# ===================================================================
# Custom TokenObtainPairView
# ===================================================================

@extend_schema(
    tags=["Auth"],
    summary="기본 JWT 로그인",
    description="Django SimpleJWT 기반 로그인. username 또는 email로 로그인 가능.",
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "username": {"type": "string"},
                "password": {"type": "string"},
            },
            "required": ["username", "password"]
        }
    },
    responses={
        200: OpenApiResponse(description="로그인 성공"),
        401: OpenApiResponse(description="로그인 실패")
    }
)
class CustomTokenObtainPairView(TokenObtainPairView):
    def post(self, request, *args, **kwargs):
        response = super().post(request, *args, **kwargs)

        if response.status_code == 200:
            try:
                login_id = request.data.get("username") or request.data.get("email")
                if login_id:
                    user = User.objects.filter(username=login_id).first()
                    if not user:
                        user = User.objects.filter(email=login_id).first()
                    if user:
                        cache.set(f"just_logged_in:{user.id}", True, timeout=60)
            except:
                pass

        return response


# ===================================================================
# Social Login
# ===================================================================

@extend_schema(
    tags=["Auth"],
    summary="소셜 로그인",
    description="provider + social_id 로 로그인하거나 신규 생성.",
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "provider": {"type": "string"},
                "socialId": {"type": "string"},
            },
            "required": ["provider", "socialId"]
        }
    },
    responses={
        200: OpenApiResponse(
            description="로그인 성공",
            examples=[
                OpenApiExample(
                    "기존 회원",
                    value={"access": "jwt", "refresh": "jwt", "needAdditionalInfo": False}
                ),
                OpenApiExample(
                    "신규 회원",
                    value={"access": "jwt", "refresh": "jwt", "needAdditionalInfo": True}
                ),
            ]
        )
    }
)
class SocialLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

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
            user = User.objects.create_user(
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


# ===================================================================
# MeView (GET/UPDATE)
# ===================================================================

@extend_schema(
    tags=["User"],
    summary="내 정보 조회",
    responses={200: UserSerializer}
)
@extend_schema(
    tags=["User"],
    methods=["PATCH"],
    summary="내 정보 수정",
    request=UserUpdateSerializer,
    responses={200: UserSerializer},
)
class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        u = request.user
        return Response({
            "id": u.id,
            "uuid": str(getattr(u, "uuid", "")),
            "email": u.email,
            "username": u.username,
            "phone": getattr(u, "phone", ""),
            "birth_date": getattr(u, "birth_date", ""),
            "gender": getattr(u, "gender", ""),
            "height": getattr(u, "height", ""),
            "weight": getattr(u, "weight", ""),
            "preferences": getattr(u, "preferences", {}),
            "prot_email": getattr(u, "prot_email", ""),
            "relation": getattr(u, "relation", ""),
            "is_active": u.is_active,
            "is_staff": u.is_staff,
            "created_at": getattr(u, "created_at", ""),
            "updated_at": getattr(u, "updated_at", ""),
            "last_login": getattr(u, "last_login", ""),
            "fcm_token": getattr(u, "fcm_token", ""),
        })

    def patch(self, request):
        user = request.user
        serializer = UserUpdateSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(UserSerializer(user).data)


# ===================================================================
# Register FCM Token
# ===================================================================

@extend_schema(
    tags=["User"],
    summary="FCM 토큰 등록",
    request={
        "application/json": {
            "type": "object",
            "properties": {"fcm_token": {"type": "string"}},
            "required": ["fcm_token"]
        }
    },
    responses={
        200: OpenApiResponse(description="등록 성공"),
        400: OpenApiResponse(description="fcm_token 누락")
    }
)
class RegisterFcmTokenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        token = request.data.get("fcm_token")
        if not token:
            return Response({"detail": "fcm_token 누락"}, status=400)

        user = request.user
        user.fcm_token = token
        user.save(update_fields=["fcm_token"])

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


# ===================================================================
# Send Email Code
# ===================================================================

@extend_schema(
    tags=["Auth"],
    summary="이메일 인증코드 발송",
    request={
        "application/json": {
            "type": "object",
            "properties": {"email": {"type": "string"}},
            "required": ["email"]
        }
    },
    responses={200: OpenApiResponse(description="발송 성공")}
)
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


# ===================================================================
# Verify Email Code
# ===================================================================

@extend_schema(
    tags=["Auth"],
    summary="이메일 인증코드 검증",
    request={
        "application/json": {
            "type": "object",
            "properties": {
                "email": {"type": "string"},
                "code": {"type": "string"},
            },
            "required": ["email", "code"]
        }
    },
    responses={
        200: OpenApiResponse(description="인증 성공"),
        400: OpenApiResponse(description="코드 불일치/만료")
    }
)
class VerifyEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")
        code = request.data.get("code")

        if not email or not code:
            return Response({"detail": "email/code 필요"}, status=400)

        saved = cache.get(f"email_code:{email}")
        if saved is None:
            return Response({"detail": "코드 없음 또는 만료"}, status=400)

        if str(saved) != str(code):
            return Response({"detail": "코드 불일치"}, status=400)

        cache.set(f"email_verified:{email}", True, timeout=300)
        return Response({"detail": "인증 성공"})


# ===================================================================
# Signup
# ===================================================================

@extend_schema(
    tags=["Auth"],
    summary="회원가입",
    request=UserCreateSerializer,
    responses={
        201: OpenApiResponse(description="회원가입 성공"),
        400: OpenApiResponse(description="유효성 실패 / 인증 미완료")
    }
)
class SignupView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")

        if not cache.get(f"email_verified:{email}"):
            return Response({"detail": "이메일 인증이 필요합니다."}, status=400)

        serializer = UserCreateSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        try:
            user = serializer.save()
        except IntegrityError as e:
            if "email" in str(e).lower():
                return Response({"detail": "이미 존재하는 이메일입니다."}, status=400)
            if "phone" in str(e).lower():
                return Response({"detail": "이미 존재하는 전화번호입니다."}, status=400)
            return Response({"detail": "회원가입 오류"}, status=400)

        cache.delete(f"email_verified:{email}")
        cache.delete(f"email_code:{email}")

        return Response({"message": "회원가입 성공", "user_id": user.id}, status=201)


# ===================================================================
# Withdrawal
# ===================================================================

@extend_schema(
    tags=["User"],
    summary="회원 탈퇴",
    responses={200: OpenApiResponse(description="탈퇴 완료")}
)
class WithdrawalView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        request.user.delete()
        return Response({"message": "회원 탈퇴 완료"})
