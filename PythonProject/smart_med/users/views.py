# users/views.py
from rest_framework.permissions import AllowAny
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth import authenticate
from rest_framework_simplejwt.tokens import RefreshToken
from .serializers import UserCreateSerializer
from rest_framework.permissions import IsAuthenticated
from .serializers import UserCreateSerializer


class SignupView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        serializer = UserCreateSerializer(data=request.data)
        if not serializer.is_valid():
            # 서버 콘솔에 찍기
            print("SIGNUP ERRORS:", serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        user = serializer.save()
        return Response({"message":"회원가입 성공","user_id":user.id,"email":user.email}, status=201)


class LoginView(APIView):
    authentication_classes = []          # CSRF/세션 영향 제거
    permission_classes = [AllowAny]

    def post(self, request):
        # 어떤 키로 와도 받기
        identifier = (
            request.data.get("email")
            or request.data.get("username")
            or request.data.get("id")
        )
        password = request.data.get("password") or request.data.get("pw")

        if not identifier or not password:
            return Response({"detail": "아이디/비밀번호 누락"}, status=status.HTTP_400_BAD_REQUEST)

        # 기본 백엔드는 username 키만 인식
        user = authenticate(request, username=identifier, password=password)
        if user is None:
            return Response({"detail": "인증 실패"}, status=status.HTTP_401_UNAUTHORIZED)

        refresh = RefreshToken.for_user(user)
        return Response(
            {
                "message": "로그인 성공",
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "user": {"id": user.id, "email": getattr(user, "email", None), "username": user.get_username()},
            },
            status=status.HTTP_200_OK,
        )

class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        return Response({
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
            "last_login": getattr(user, "last_login", "")
        }, status=status.HTTP_200_OK)