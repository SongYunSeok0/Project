import os
from django.apps import AppConfig

class RagConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "rag"

    def ready(self):
      # runserver 자동 재실행 때문에 두 번 로드되는 것 방지
      if os.environ.get("RUN_MAIN") == "true":
          from .llm import preload_qwen
          #preload_qwen()  #1127임시주석

class HealthConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "health"
