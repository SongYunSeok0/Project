import os
from pathlib import Path
import environ

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


# CSRF 허용 출처 (개발용)
CSRF_TRUSTED_ORIGINS = [
    "http://localhost:8000",
    "http://127.0.0.1:8000",
    "http://localhost:5173",   # 프론트 개발 서버 예시(Vite 등)
    "http://10.0.2.2:8000",    # 안드로이드 에뮬레이터→호스트
]

# CORS 허용 출처 (개발용)
CORS_ALLOWED_ORIGINS = [
    "http://localhost:8000",
    "http://127.0.0.1:8000",
    "http://localhost:5173",
    "http://10.0.2.2:8000",
]
CORS_ALLOW_CREDENTIALS = True

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

    "pgvector.django",   # ★ pgvector
    "rag",               # ★ RAG 앱
    "users", "medications", "iot", "health",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",

    "corsheaders.middleware.CorsMiddleware",  # ★ CommonMiddleware보다 위
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
        "NAME": env("POSTGRES_DB", default="RNB"),
        "USER": env("POSTGRES_USER", default="postgres"),
        "PASSWORD": env("POSTGRES_PASSWORD", default="1234"),
        "HOST": env("POSTGRES_HOST", default="127.0.0.1"),  # localhost 대신 127.0.0.1 권장
        "PORT": env("POSTGRES_PORT", default="5432"),
        "OPTIONS": {
            "options": "-c client_encoding=UTF8",
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

CELERY_BROKER_URL = 'redis://localhost:6379/0'
CELERY_RESULT_BACKEND = 'redis://localhost:6379/0'

AUTH_USER_MODEL = "users.User"
