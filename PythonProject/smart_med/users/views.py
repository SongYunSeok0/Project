# users/views.py
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth import authenticate
from rest_framework_simplejwt.tokens import RefreshToken
from .serializers import UserCreateSerializer
from smart_med.firebase import send_fcm_to_token  # smart_med/firebase.py 에 있다고 가정




class SignupView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        serializer = UserCreateSerializer(data=request.data)
        if not serializer.is_valid():
            print("SIGNUP ERRORS:", serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        user = serializer.save()
        return Response(
            {"message": "회원가입 성공", "user_id": user.id, "email": user.email},
            status=201,
        )


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
            "email",
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
