FROM python:3.10 AS base
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
WORKDIR /app

# 1. 라이브러리 설치 (캐시 활용을 위해 먼저 수행)
COPY requirements.txt /app/
# --no-cache-dir로 이미지 크기 최소화
RUN pip install --upgrade pip && pip install --no-cache-dir -r requirements.txt

# 2. 소스코드 복사
COPY . /app/

# ====================
# Web service image
# ====================
FROM base AS web
# docker-compose에서 command를 덮어쓰므로 여기 CMD는 기본값 역할만 합니다.
CMD ["gunicorn", "smart_med.wsgi:application", "--bind", "0.0.0.0:8000"]

# ====================
# Celery worker image
# ====================
FROM base AS celery
CMD ["celery", "-A", "smart_med", "worker", "--loglevel=info"]

# ====================
# Celery beat image
# ====================
FROM base AS celery_beat
CMD ["celery", "-A", "smart_med", "beat", "--loglevel=info"]