# users/views.py
import secrets

from django.core.mail import send_mail
from django.core.cache import cache
from django.conf import settings
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth import authenticate, get_user_model
from rest_framework_simplejwt.tokens import RefreshToken
from .serializers import UserCreateSerializer, UserUpdateSerializer, UserSerializer
from smart_med.firebase import send_fcm_to_token  # smart_med/firebase.py 에 있다고 가정
User = get_user_model()




class LoginView(APIView):
    authentication_classes = []
    permission_classes = [AllowAny]

    def post(self, request):
        identifier = (
            request.data.get("email")
            or request.data.get("username")
            or request.data.get("id")
        )
        password = request.data.get("password") or request.data.get("pw")

        if not identifier or not password:
            return Response({"detail": "아이디/비밀번호 누락"}, status=status.HTTP_400_BAD_REQUEST)

        user = authenticate(request, username=identifier, password=password)
        if user is None:
            return Response({"detail": "인증 실패"}, status=status.HTTP_401_UNAUTHORIZED)

        refresh = RefreshToken.for_user(user)

        # 여기 추가
        print("LOGIN user fcm_token =", getattr(user, "fcm_token", None))

        # ✅ 로그인 성공 시, fcm_token 있으면 푸시
        if getattr(user, "fcm_token", None):
            send_fcm_to_token(
                token=user.fcm_token,
                title="로그인 알림",
                body=f"{user.username} 님이 로그인했습니다.",
            )
        return Response(
            {
                "message": "로그인 성공",
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "user": {
                    "id": user.id,
                    "email": getattr(user, "email", None),
                    "username": user.get_username(),
                },
            },
            status=status.HTTP_200_OK,
        )


class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        return Response(
            {
                "id": user.id,
                "uuid": str(getattr(user, "uuid", "")),
                "email": user.email,
                "username": user.username,
                "phone": getattr(user, "phone", ""),
                "birth_date": getattr(user, "birth_date", ""),
                "gender": getattr(user, "gender", ""),
                "height": getattr(user, "height", ""),
                "weight": getattr(user, "weight", ""),
                "preferences": getattr(user, "preferences", {}),
                "prot_phone": getattr(user, "prot_phone", ""),
                "relation": getattr(user, "relation", ""),
                "is_active": user.is_active,
                "is_staff": user.is_staff,
                "created_at": getattr(user, "created_at", ""),
                "updated_at": getattr(user, "updated_at", ""),
                "last_login": getattr(user, "last_login", ""),
                "fcm_token": getattr(user, "fcm_token", ""),
            },
            status=status.HTTP_200_OK,
        )
    def patch(self, request):
        user = request.user
        allowed_fields = {
            "username",
            "phone",
            "birth_date",
            "gender",
            "height",
            "weight",
            "preferences",
            "prot_phone",
            "relation",
        }
        data = request.data
        updated = False
        for field, value in data.items():
            if field in allowed_fields:
                setattr(user, field, value)
                updated = True
        if updated:
            user.save()
            return Response({"detail": "updated"}, status=status.HTTP_200_OK)
        else:
            return Response({"detail": "업데이트 가능한 필드 없음"}, status=status.HTTP_400_BAD_REQUEST)


    def patch(self, request):
        user = request.user

        serializer = UserUpdateSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        # 여기서 UserSerializer로 전체 데이터 반환!
        return Response(UserSerializer(user).data, status=status.HTTP_200_OK)


# ✅ FCM 토큰 등록용 API
class RegisterFcmTokenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        token = request.data.get("fcm_token")
        if not token:
            return Response({"detail": "fcm_token 누락"}, status=status.HTTP_400_BAD_REQUEST)

        user = request.user
        user.fcm_token = token
        user.save(update_fields=["fcm_token"])

        # ✅ 여기서 바로 푸시 전송
        send_fcm_to_token(
            token=token,
            title="로그인 알림",
            body=f"{user.username} 님이 로그인했습니다.",
        )

        return Response({"detail": "ok"}, status=status.HTTP_200_OK)

# ================================
# 1) 인증코드 발송
# ================================
class SendEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")

        if not email:
            return Response({"detail": "email 필요"}, status=400)

        # 6자리 인증코드 생성
        code = secrets.randbelow(900000) + 100000

        # 3분간 유효한 코드 저장
        cache.set(f"email_code:{email}", code, timeout=180)

        # 이메일 전송
        send_mail(
            "이메일 인증코드",
            f"인증코드: {code}\n3분 안에 입력해주세요.",
            settings.EMAIL_HOST_USER,
            [email],
        )

        return Response({"detail": "인증코드가 발송되었습니다."}, status=200)

# ================================
# 2) 인증코드 검증
# ================================
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

        # 성공 시 "인증됨" 상태 저장 (5분)
        cache.set(f"email_verified:{email}", True, timeout=300)

        return Response({"detail": "인증 성공"}, status=200)

# ================================
# 3) 회원가입
# ================================
class SignupView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")

        # 이메일 인증 여부 확인
        if not cache.get(f"email_verified:{email}"):
            return Response({"detail": "이메일 인증이 필요합니다."}, status=400)

        serializer = UserCreateSerializer(data=request.data)

        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        user = serializer.save()

        # 인증 완료 후 캐시 삭제
        cache.delete(f"email_verified:{email}")
        cache.delete(f"email_code:{email}")

        return Response(
            {"message": "회원가입 성공", "user_id": user.id},
            status=201
        )
class WithdrawalView(APIView):
    permission_classes = [IsAuthenticated] # 로그인한 사람만 가능

    def delete(self, request):
        user = request.user
        user.delete() # DB에서 CASCADE로 연쇄 삭제됨
        return Response({"message": "회원 탈퇴 완료"}, status=status.HTTP_200_OK)