# users/views.py
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
from django.contrib.auth import authenticate, get_user_model
from rest_framework_simplejwt.tokens import RefreshToken
from .serializers import UserCreateSerializer, UserUpdateSerializer, UserSerializer
from rest_framework_simplejwt.views import TokenObtainPairView
from notifications.services import send_fcm_to_token
from rest_framework.decorators import api_view, permission_classes

User = get_user_model()


# ===========================================================================
# ✅ [추가] 기본 로그인(/api/token/)을 대체할 커스텀 뷰
# ===========================================================================
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
                        print("Access Token:", request.META.get("HTTP_AUTHORIZATION"))
                        cache.set(f"just_logged_in:{user.id}", True, timeout=60)
            except Exception as e:
                print(f"[CustomLogin] Error setting cache: {e}")

        return response

class SocialLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        print("[SocialLoginView] POST request received")
        provider = request.data.get("provider")
        social_id = request.data.get("socialId")

        # 신규회원 여부 확인 - 기존회원의 경우
        try:
            user = User.objects.get(provider=provider, social_id=social_id)
            print(f"[SocialLoginView] Existing user found: {user.id}")
            # JWT 발급
            refresh = RefreshToken.for_user(user)

            # ✅ [핵심 1-1] 소셜 로그인도 마찬가지로 표식을 남깁니다.
            cache_key = f"just_logged_in:{user.id}"
            cache.set(cache_key, True, timeout=60)
            print(f"[SocialLoginView] Cache set: key='{cache_key}', value=True")

            return Response({
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "needAdditionalInfo": False
            }, status=200)

        except User.DoesNotExist:
            print("[SocialLoginView] User does not exist (New User)")
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

        serializer = UserUpdateSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        return Response(UserSerializer(user).data, status=status.HTTP_200_OK)


# FCM 토큰 등록용 API
class RegisterFcmTokenView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        print("[RegisterFcmTokenView] POST request received")
        token = request.data.get("fcm_token")
        if not token:
            print("[RegisterFcmTokenView] Error: fcm_token missing in request data")
            return Response({"detail": "fcm_token 누락"}, status=status.HTTP_400_BAD_REQUEST)

        user = request.user
        print(f"[RegisterFcmTokenView] User: {user.id}, Token: {token[:20]}...")

        user.fcm_token = token
        user.save(update_fields=["fcm_token"])
        print("[RegisterFcmTokenView] Token saved to DB")

        # [핵심 2] "방금 로그인했니?" 확인 후 알림 발송
        cache_key = f"just_logged_in:{user.id}"
        is_just_logged_in = cache.get(cache_key)
        print(f"[RegisterFcmTokenView] Checking cache key='{cache_key}'. Result: {is_just_logged_in}")

        if is_just_logged_in:
            print("[RegisterFcmTokenView] 'Just Logged In' flag found! Sending notification...")
            try:
                res = send_fcm_to_token(
                    token=token,
                    title="로그인 알림",
                    body=f"{user.username} 님이 로그인했습니다.",
                )
                print(f"[RegisterFcmTokenView] Notification sent result: {res}")
            except Exception as e:
                print(f"[RegisterFcmTokenView] Failed to send notification: {e}")
                traceback.print_exc()

            # 중복 발송 방지를 위해 표식 즉시 제거
            cache.delete(cache_key)
            print(f"[RegisterFcmTokenView] Cache key '{cache_key}' deleted.")
        else:
            print("[RegisterFcmTokenView] 'Just Logged In' flag NOT found. Skipping notification.")

        return Response({"detail": "ok"}, status=status.HTTP_200_OK)



# ================================
# ✅ 이메일 중복 체크 (NEW!)
# ================================
@api_view(['POST'])
@permission_classes([AllowAny])
def check_email_duplicate(request):
    """이메일 중복 체크 - CSRF 면제"""
    print("[CheckEmail] ========== 요청 받음 ==========")

    email = request.data.get("email")
    print(f"[CheckEmail] Email: {email}")

    if not email:
        print("[CheckEmail] ❌ 이메일 없음")
        return Response(
            {"detail": "email 필드가 필요합니다."},
            status=status.HTTP_400_BAD_REQUEST
        )

    exists = User.objects.filter(email=email).exists()
    print(f"[CheckEmail] Exists: {exists}")

    return Response({"exists": exists}, status=status.HTTP_200_OK)


# ================================
# 1) 인증코드 발송 (회원가입 & 보호자 인증 공용)
# ================================
class SendEmailCodeView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get("email")
        name = request.data.get("name")  # 보호자 이름 (선택 사항)

        if not email:
            return Response({"detail": "email 필요"}, status=400)

        # ---------------------------------------------------------
        # [로직 추가] name이 들어왔다면? -> 보호자 등록용 인증 요청
        # ---------------------------------------------------------
        if name:
            # DB에서 이메일과 이름이 일치하는 유저가 있는지 확인
            # (보호자가 우리 앱 사용자인 경우에만 등록 가능하다는 정책이라면)
            user_exists = User.objects.filter(email=email, username=name).exists()

            if not user_exists:
                return Response(
                    {"detail": "해당 이름과 이메일을 가진 사용자를 찾을 수 없습니다."},
                    status=404
                )

        # ---------------------------------------------------------
        # 공통 로직 (인증코드 생성 및 발송)
        # ---------------------------------------------------------
        code = secrets.randbelow(900000) + 100000  # 100000 ~ 999999

        # Redis 캐시에 저장 (3분 유효)
        # 키를 구분하고 싶다면 보호자용은 접두사를 다르게 할 수도 있지만,
        # 단순 인증 목적이라면 덮어씌워도 무방합니다.
        cache.set(f"email_code:{email}", code, timeout=180)

        # 메일 발송
        try:
            send_mail(
                "[MyRhythm] 이메일 인증코드",
                f"인증코드: {code}\n3분 안에 입력해주세요.",
                settings.EMAIL_HOST_USER,
                [email],
                fail_silently=False,
            )
        except Exception as e:
            return Response({"detail": f"메일 전송 실패: {str(e)}"}, status=500)

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
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        user = request.user
        user.delete()
        return Response({"message": "회원 탈퇴 완료"}, status=status.HTTP_200_OK)