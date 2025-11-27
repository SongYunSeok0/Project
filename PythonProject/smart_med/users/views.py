# users/views.py
import secrets
import logging

from django.core.mail import send_mail
from django.core.cache import cache
from django.conf import settings
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth import authenticate, get_user_model
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView

from .serializers import UserCreateSerializer, UserUpdateSerializer, UserSerializer
from smart_med.firebase import send_fcm_to_token  # smart_med/firebase.py 에 있다고 가정
from django.db import IntegrityError
import traceback


logger = logging.getLogger(__name__)

User = get_user_model()
class CustomTokenObtainPairView(TokenObtainPairView):
    def post(self, request, *args, **kwargs):
        # 1. 기본 로직 실행 (토큰 발급)
        response = super().post(request, *args, **kwargs)

        # 2. 로그인 성공(200) 했다면 -> "방금 로그인함" 표식 남기기
        if response.status_code == 200:
            try:
                # 요청 데이터에서 아이디(username 또는 email) 꺼내기
                login_id = request.data.get("username") or request.data.get("email") or request.data.get("id")

                if login_id:
                    # DB에서 유저 찾기 (username 필드나 email 필드로 조회)
                    user = User.objects.filter(username=login_id).first()
                    if not user:
                        user = User.objects.filter(email=login_id).first()

                    if user:
                        print(f"[CustomLogin] User {user.id} logged in via /api/token/. Setting cache.")
                        cache.set(f"just_logged_in:{user.id}", True, timeout=60)
            except Exception as e:
                print(f"[CustomLogin] Error setting cache: {e}")

        return response


# class LoginView(APIView):
#     authentication_classes = []
#     permission_classes = [AllowAny]
#
#     def post(self, request):
#         print("[LoginView] POST request received")  # 로그 추가
#         identifier = (
#                 request.data.get("email")
#                 or request.data.get("username")
#                 or request.data.get("id")
#         )
#         password = request.data.get("password") or request.data.get("pw")
#
#         if not identifier or not password:
#             print("[LoginView] Missing identifier or password")  # 로그 추가
#             return Response({"detail": "아이디/비밀번호 누락"}, status=status.HTTP_400_BAD_REQUEST)
#
#         user = authenticate(request, username=identifier, password=password)
#         if user is None:
#             print(f"[LoginView] Authentication failed for: {identifier}")  # 로그 추가
#             return Response({"detail": "인증 실패"}, status=status.HTTP_401_UNAUTHORIZED)
#
#         print(f"[LoginView] Login successful for user: {user.id} ({user.username})")  # 로그 추가
#         refresh = RefreshToken.for_user(user)
#
#         # ✅ [핵심 1] 여기서 직접 보내지 않고, "방금 로그인함" 표식만 남깁니다. (유효시간 60초)
#         cache_key = f"just_logged_in:{user.id}"
#         cache.set(cache_key, True, timeout=60)
#         print(f"[LoginView] Cache set: key='{cache_key}', value=True (timeout=60s)")  # 로그 추가
#
#         return Response(
#             {
#                 "message": "로그인 성공",
#                 "access": str(refresh.access_token),
#                 "refresh": str(refresh),
#                 "user": {
#                     "id": user.id,
#                     "email": getattr(user, "email", None),
#                     "username": user.get_username(),
#                 },
#             },
#             status=status.HTTP_200_OK,
#         )

class SocialLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        provider = request.data.get("provider")
        social_id = request.data.get("socialId")

        # 신규회원 여부 확인 - 기존회원의 경우
        try:
            user = User.objects.get(provider=provider, social_id=social_id)
            # JWT 발급
            # 구글은 아이디 토큰 jwt 안에 정도가 들어있어서 토큰하나적어두면 알아서받아옴
            refresh = RefreshToken.for_user(user)
            return Response({
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "needAdditionalInfo": False
            }, status=200)

        except User.DoesNotExist:
            # 신규유저는 프로바이더+소셜아이디 데이터베이스에 저장
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
                "needAdditionalInfo": True,
            }, status=200)


class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        logger.info(f"[MeView][GET] user={user} (id={user.id}, email={user.email})")
        logger.info(f"[MeView][GET] headers={request.headers}")

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
                "prot_email": getattr(user, "prot_email", ""),
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

        logger.info(f"[MeView][PATCH] user={user} (id={user.id})")
        logger.info(f"[MeView][PATCH] request.data = {request.data}")
        logger.info(f"[MeView][PATCH] headers={request.headers}")

        serializer = UserUpdateSerializer(user, data=request.data, partial=True)

        if not serializer.is_valid():
            logger.error(f"[MeView][PATCH] serializer errors: {serializer.errors}")
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        serializer.save()

        logger.info(f"[MeView][PATCH] Update Success for user_id={user.id}")

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


        try:
            user = serializer.save()
        except IntegrityError as e:
            # 중복 이메일, 중복 전화번호 등
            if "email" in str(e).lower():
                return Response({"detail": "이미 존재하는 이메일입니다."}, status=400)
            if "phone" in str(e).lower():
                return Response({"detail": "이미 존재하는 전화번호입니다."}, status=400)
            return Response({"detail": "회원가입 중 오류가 발생했습니다."}, status=400)
        except Exception as e:
            print("===== SIGNUP ERROR TRACEBACK =====")
            traceback.print_exc()
            print("==================================")
            return Response({"detail": str(e)}, status=400)

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