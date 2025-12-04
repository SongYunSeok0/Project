# auth/docs.py
from drf_spectacular.utils import (
    extend_schema,
    OpenApiParameter,
    OpenApiResponse,
    OpenApiExample
)
from .serializers import UserCreateSerializer, UserUpdateSerializer, UserSerializer

# ==========================================================
#  JWT 로그인
# ==========================================================

jwt_login_docs = extend_schema(
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


# ==========================================================
#  소셜 로그인
# ==========================================================

social_login_docs = extend_schema(
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


# ==========================================================
# MeView
# ==========================================================

me_get_docs = extend_schema(
    tags=["User"],
    summary="내 정보 조회",
    responses={200: UserSerializer}
)

me_patch_docs = extend_schema(
    tags=["User"],
    methods=["PATCH"],
    summary="내 정보 수정",
    request=UserUpdateSerializer,
    responses={200: UserSerializer}
)


# ==========================================================
# FCM 등록
# ==========================================================

register_fcm_docs = extend_schema(
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


# ==========================================================
# Email 인증코드 전송
# ==========================================================

send_email_code_docs = extend_schema(
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


# ==========================================================
# Email 인증코드 검증
# ==========================================================

verify_email_code_docs = extend_schema(
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


# ==========================================================
# 회원가입
# ==========================================================

signup_docs = extend_schema(
    tags=["Auth"],
    summary="회원가입",
    request=UserCreateSerializer,
    responses={
        201: OpenApiResponse(description="회원가입 성공"),
        400: OpenApiResponse(description="유효성 실패 / 인증 미완료")
    }
)


# ==========================================================
# 회원탈퇴
# ==========================================================

withdraw_docs = extend_schema(
    tags=["User"],
    summary="회원 탈퇴",
    responses={200: OpenApiResponse(description="탈퇴 완료")}
)
