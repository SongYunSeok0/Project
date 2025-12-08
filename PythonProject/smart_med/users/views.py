# users/views.py
import secrets
import traceback

from django.core.mail import send_mail
from django.core.cache import cache
from django.conf import settings
from django.db import IntegrityError
from django.contrib.auth import get_user_model

from rest_framework.permissions import AllowAny, IsAuthenticated, BasePermission
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, generics

from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView

from notifications.services import send_fcm_to_token
from rest_framework.decorators import api_view, permission_classes

from .serializers import (
    UserCreateSerializer,
    UserUpdateSerializer,
    UserSerializer,
)
from .docs import (
    jwt_login_docs, social_login_docs,
    me_get_docs, me_patch_docs,
    register_fcm_docs, send_email_code_docs,
    verify_email_code_docs, signup_docs, withdraw_docs,
)

User = get_user_model()


# ============================================================
# ✔ 커스텀 권한: is_staff 사용자만 접근 가능
# ============================================================

class IsStaffUser(BasePermission):
    """Django user.is_staff == True 인 경우만 허용"""
    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.is_staff
        )


# ============================================================
# ✔ Custom JWT Login (/api/token/)
# ============================================================

@jwt_login_docs
class CustomTokenObtainPairView(TokenObtainPairView):
    """JWT 로그인 → 로그인 성공 시 Redis 캐시에 'just_logged_in' 저장"""

    def post(self, request, *args, **kwargs):
        response = super().post(request, *args, **kwargs)

        if response.status_code == 200:
            login_id = (
                request.data.get("username")
                or request.data.get("email")
                or request.data.get("id")
            )

            try:
                user = (
                    User.objects.filter(username=login_id).first()
                    or User.objects.filter(email=login_id).first()
                )

                if user:
                    cache_key = f"just_logged_in:{user.id}"
                    cache.set(cache_key, True, timeout=60)
                    print(f"[CustomLogin] Set {cache_key}=True")
            except Exception:
                traceback.print_exc()

        return response


# ============================================================
# ✔ SOCIAL LOGIN
# ============================================================

@social_login_docs
class SocialLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    @staticmethod
    def _generate_username(base):
        username = base
        count = 1
        while User.objects.filter(username=username).exists():
            username = f"{base}_{count}"
            count += 1
        return username

    def post(self, request):
        provider = request.data.get("provider")
        social_id = request.data.get("socialId")

        if not provider or not social_id:
            return Response({"detail": "provider/socialId 필요"}, status=400)

        # 기존 유저 존재?
        user = User.objects.filter(provider=provider, social_id=social_id).first()

        if user:
            refresh = RefreshToken.for_user(user)

            cache_key = f"just_logged_in:{user.id}"
            cache.set(cache_key, True, timeout=60)
            print(f"[SocialLogin] Existing user {user.id}, set {cache_key}=True")

            return Response({
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "needAdditionalInfo": False,
            })

        # 신규 유저 생성
        base = f"{provider}_{social_id}"
        username = self._generate_username(base)

        user = User.objects.create_user(
            username=username,
            provider=provider,
            social_id=social_id,
            email=None,
            password=None,
        )

        refresh = RefreshToken.for_user(user)
        print(f"[SocialLogin] New user created: {user.id}")

        return Response({
            "access": str(refresh.access_token),
            "refresh": str(refresh),
            "needAdditionalInfo": True,
        })


# ============================================================
# ✔ MeView — 사용자 정보 조회 & 수정
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
# ✔ Register FCM Token
# ============================================================

@register_fcm_docs
class RegisterFcmTokenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        token = request.data.get("fcm_token")

        if not token:
            return Response({"detail": "fcm_token 누락"}, status=400)

        user = request.user
        user.fcm_token = token
        user.save(update_fields=["fcm_token"])

        cache_key = f"just_logged_in:{user.id}"
        is_just_logged_in = cache.get(cache_key)

        if is_just_logged_in:
            try:
                send_fcm_to_token(
                    token=token,
                    title="로그인 알림",
                    body=f"{user.username} 님이 로그인했습니다.",
                )
            except Exception:
                traceback.print_exc()

            cache.delete(cache_key)

        return Response({"detail": "ok"})


# ============================================================
# ✔ 이메일 중복 체크
# ============================================================

@api_view(['POST'])
@permission_classes([AllowAny])
def check_email_duplicate(request):
    email = request.data.get("email")
    if not email:
        return Response({"detail": "email 필요"}, status=400)

    exists = User.objects.filter(email=email).exists()
    return Response({"exists": exists})


# ============================================================
# ✔ 이메일 인증코드 발송
# ============================================================

@send_email_code_docs
class SendEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")
        name = request.data.get("name")  # 보호자 등록 시 사용

        if not email:
            return Response({"detail": "email 필요"}, status=400)

        # 보호자 인증일 때 이름 매칭 체크
        if name:
            if not User.objects.filter(email=email, username=name).exists():
                return Response({"detail": "해당 사용자를 찾을 수 없습니다."}, status=404)

        # 코드 생성
        code = secrets.randbelow(900000) + 100000
        cache.set(f"email_code:{email}", code, timeout=180)

        send_mail(
            "[MyRhythm] 이메일 인증코드",
            f"인증코드: {code}\n3분 안에 입력해주세요.",
            settings.EMAIL_HOST_USER,
            [email],
        )

        return Response({"detail": "인증코드가 발송되었습니다."})


# ============================================================
# ✔ 이메일 코드 검증
# ============================================================

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


# ============================================================
# ✔ Signup (회원가입)
# ============================================================

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
        except IntegrityError as e:
            if "email" in str(e).lower():
                return Response({"detail": "이미 존재하는 이메일입니다."}, status=400)
            if "phone" in str(e).lower():
                return Response({"detail": "이미 존재하는 전화번호입니다."}, status=400)
            return Response({"detail": "회원가입 오류"}, status=400)

        cache.delete(f"email_verified:{email}")
        cache.delete(f"email_code:{email}")

        return Response({"message": "회원가입 성공", "user_id": user.id}, status=201)


# ============================================================
# ✔ 회원 탈퇴
# ============================================================

@withdraw_docs
class WithdrawalView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        request.user.delete()
        return Response({"message": "회원 탈퇴 완료"})


# ============================================================
# ✔ 관리자용: 사용자 목록 / 상세 조회 (is_staff)
# ============================================================

class UserListView(generics.ListAPIView):
    """
    GET /api/users/ → 전체 사용자 목록
    is_staff=True 인 계정만 접근 가능
    """
    queryset = User.objects.all().order_by('-created_at')  # 🔥 date_joined → created_at
    serializer_class = UserSerializer
    permission_classes = [IsStaffUser]


class UserDetailView(generics.RetrieveAPIView):
    """
    GET /api/users/<id>/ → 특정 사용자 상세
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [IsStaffUser]
    lookup_field = 'id'
