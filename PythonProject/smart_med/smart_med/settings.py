import os
from pathlib import Path
import environ
from datetime import timedelta


BASE_DIR = Path(__file__).resolve().parent.parent

# .env 로드 (django-environ만 사용, dotenv 중복 제거)
env = environ.Env()
environ.Env.read_env(BASE_DIR / ".env")

SECRET_KEY = env("SECRET_KEY")
DEBUG = True

ALLOWED_HOSTS = ["*"]

FIREBASE_CREDENTIAL_PATH = env(
    "FIREBASE_CREDENTIAL_PATH",
    default=str(BASE_DIR / "smart_med_firebase_admin.json"),
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

    # Serializer Override 가능 (기본값 사용)
}


INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",

    "rest_framework",
    "drf_yasg",
    "corsheaders",
    "django_extensions",

    "users", "medications", "iot", "health","rag.apps.RagConfig"
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",

    # CORS는 최대한 위쪽, CommonMiddleware보다 먼저
    "corsheaders.middleware.CorsMiddleware",

    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "smart_med.urls"

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
        "NAME": env("POSTGRES_DB", default="RNB2"),
        "USER": env("POSTGRES_USER", default="postgres"),
        "PASSWORD": env("POSTGRES_PASSWORD", default="1234"),
        # ← 연결 이슈 피하려면 localhost 대신 127.0.0.1 권장
        "HOST": env("POSTGRES_HOST", default="127.0.0.1"),
        "PORT": env("POSTGRES_PORT", default="5432"),
        "OPTIONS": {
            # psycopg2가 DSN을 UTF-8로 고정해 읽도록
            "options": "-c client_encoding=UTF8",
            # 선택: 앱 이름을 ASCII로 고정(경로/로캘 관련 문제 회피)
            "application_name": "django",
        },
    }
}

REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": (
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ),
    "DEFAULT_PERMISSION_CLASSES": (
        "rest_framework.permissions.IsAuthenticated",
    ),
}

# i18n
LANGUAGE_CODE = "ko-kr"
TIME_ZONE = "Asia/Seoul"
USE_I18N = True
USE_TZ = True

STATIC_URL = "static/"

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

CELERY_BROKER_URL = "redis://localhost:6379/0"
CELERY_RESULT_BACKEND = "redis://localhost:6379/0"

AUTH_USER_MODEL = "users.User"