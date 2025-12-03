FROM python:3.10
# Python 환경 설정
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

WORKDIR /app
COPY requirements.txt /app/
RUN pip install --upgrade pip && pip install -r requirements.txt
COPY . /app/
CMD ["gunicorn", "smart_med.wsgi:application", "--bind", "0.0.0.0:8000"]
