FROM python:3.10

# Python 환경 설정
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

WORKDIR /app

# requirements 설치
COPY requirements.txt /app/
RUN pip install --upgrade pip && pip install -r requirements.txt

# 프로젝트 복사
COPY . /app/

# Gunicorn 실행
CMD ["gunicorn", "smart_med.wsgi:application", "--bind", "0.0.0.0:8000"]
