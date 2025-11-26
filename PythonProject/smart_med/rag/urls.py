from django.urls import path
from .views import DrugRAGView, RAGTaskResultView

urlpatterns = [
    path("drug/", DrugRAGView.as_view()),
    path("task/<str:task_id>/", RAGTaskResultView.as_view()),
]
