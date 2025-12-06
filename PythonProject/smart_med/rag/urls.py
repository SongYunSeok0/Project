from django.urls import path
from .views import DrugRAGView, RAGTaskResultView

urlpatterns = [
    path("drug/", DrugRAGView.as_view()),
    path("result/<str:task_id>/", RAGTaskResultView.as_view()),
]
