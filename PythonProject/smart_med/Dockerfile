FROM python:3.10 AS base
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
WORKDIR /app

COPY requirements.txt /app/
RUN pip install --upgrade pip && pip install -r requirements.txt

COPY . /app/

# ====================
# Web service image
# ====================
FROM base AS web
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

