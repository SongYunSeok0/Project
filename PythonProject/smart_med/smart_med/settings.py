import os
from pathlib import Path
from datetime import timedelta
from celery.schedules import crontab
import environ

BASE_DIR = Path(__file__).resolve().parent.parent

# === ENV 로드 ===
env = environ.Env()
environ.Env.read_env(BASE_DIR / ".env")

SECRET_KEY = env("SECRET_KEY")
DEBUG = True

ALLOWED_HOSTS = ["*"]

FIREBASE_CREDENTIAL_PATH = env(
    "FIREBASE_CREDENTIAL_PATH",
    default="/run/secrets/smart_med_firebase_admin.json"
)

# 와일드카드(*)는 CSRF_TRUSTED_ORIGINS에 허용되지 않아요.
# 실제 접근 도메인/포트로 명시해 주세요 (개발 기본 예시)
CSRF_TRUSTED_ORIGINS = [
    "http://localhost:8000",
    "http://127.0.0.1:8000",
]
SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(days=7),      # access token 7일 유지
    "REFRESH_TOKEN_LIFETIME": timedelta(days=30),    # refresh token 30일 유지
    "ROTATE_REFRESH_TOKENS": True,                  # refresh 시 새 refresh로 갱신
    "BLACKLIST_AFTER_ROTATION": True,

    "ALGORITHM": "HS256",
    "SIGNING_KEY": SECRET_KEY,

    "AUTH_HEADER_TYPES": ("Bearer",),
    "AUTH_HEADER_NAME": "HTTP_AUTHORIZATION",

    "USER_ID_FIELD": "id",
    "USER_ID_CLAIM": "user_id",
}


INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",

    "rest_framework",
    "drf_spectacular",
    "drf_spectacular_sidecar",
    "corsheaders",
    "django_extensions",
    "django_celery_results",
    "users",
    "medications",
    "iot",
    "health",
    "rag.apps.RagConfig",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",

    # CORS는 최대한 위쪽, CommonMiddleware보다 먼저
    "corsheaders.middleware.CorsMiddleware",

    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "smart_med.utils.middleware.RequestLoggingMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]
MIDDLEWARE.insert(0, "smart_med.utils.middleware.DisableChunkedMiddleware")


# === CELERY 설정 ===
CELERY_BROKER_URL = env("CELERY_BROKER_URL")
CELERY_RESULT_BACKEND = env("CELERY_RESULT_BACKEND")
CELERY_ACCEPT_CONTENT = ["json"]
CELERY_TASK_SERIALIZER = "json"
CELERY_RESULT_SERIALIZER = "json"
CELERY_TIMEZONE = "Asia/Seoul"
CELERY_ENABLE_UTC = False

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

WSGI_APPLICATION = "smart_med.wsgi.application"

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": os.getenv("POSTGRES_DB"),
        "USER": os.getenv("POSTGRES_USER"),
        "PASSWORD": os.getenv("POSTGRES_PASSWORD"),
        "HOST": os.getenv("POSTGRES_HOST", "postgres"),
        "PORT": os.getenv("POSTGRES_PORT", 5432),
        "OPTIONS": {
            "options": "-c client_encoding=UTF8",
            "application_name": "django",
        },
    }
}

# === Email ===
EMAIL_BACKEND = "django.core.mail.backends.smtp.EmailBackend"
EMAIL_HOST = "smtp.gmail.com"
EMAIL_PORT = 587
EMAIL_USE_TLS = True
EMAIL_HOST_USER = env("EMAIL_HOST_USER")
EMAIL_HOST_PASSWORD = env("EMAIL_HOST_PASSWORD")
DEFAULT_FROM_EMAIL = EMAIL_HOST_USER
# =================================

CACHES = {
    "default": {
        "BACKEND": "django.core.cache.backends.locmem.LocMemCache",
        "LOCATION": "email-verification",
    }
}

REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": (
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ),
    "DEFAULT_PERMISSION_CLASSES": (
        "rest_framework.permissions.IsAuthenticated",
    ),
    "DEFAULT_RENDERER_CLASSES": (
        "rest_framework.renderers.JSONRenderer",
    ),
    "DEFAULT_SCHEMA_CLASS": "drf_spectacular.openapi.AutoSchema",
}

SPECTACULAR_SETTINGS = {
    "TITLE": "Smart Medication Care API",
    "DESCRIPTION": "MyRythm API 명세서",
    "VERSION": "1.0.0",
    # 스키마 포함 여부
    "SERVE_INCLUDE_SCHEMA": False,
    # Request/Response 분리
    "COMPONENT_SPLIT_REQUEST": True,
    # 공통 에러 Response 생성
    "DEFAULT_GENERATE_ERROR_RESPONSE": True,
    # 사용할 수 있는 VALID 옵션
    "SCHEMA_PATH_PREFIX": "/api",
    # JWT Auth 표시
    "AUTHENTICATION_WHITELIST": [],
    # 태그 정의
    "TAGS": [
        {"name": "Auth", "description": "로그인 · 회원가입 · 이메일 인증 · 소셜 로그인"},
        {"name": "User", "description": "사용자 정보 조회 및 수정, FCM, 회원탈퇴"},
        {"name": "Health", "description": "심박수 · 걸음수 건강 데이터"},
        {"name": "IoT", "description": "IoT 기기 연동 · 센서 데이터 수집 · 명령 전달"},
        {"name": "RAG", "description": "약학 RAG 검색 · 비동기 작업 조회"},
        {"name": "RegiHistory", "description": "처방/영양제 등록 이력 관리"},
        {"name": "Plan", "description": "복약 일정 생성 · 수정 · 삭제 · 스마트 일정 생성"},
        {"name": "Notification", "description": "FCM 알림 기능"},
    ],
}



# i18n
LANGUAGE_CODE = "ko-kr"
TIME_ZONE = "Asia/Seoul"
USE_I18N = True
USE_TZ = True

STATIC_URL = "static/"
ROOT_URLCONF = "smart_med.urls"
DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

CELERY_BROKER_URL = "redis://localhost:6379/0"
CELERY_RESULT_BACKEND = "redis://localhost:6379/0"

AUTH_USER_MODEL = "users.User"