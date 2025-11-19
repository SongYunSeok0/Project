from django.urls import path
from .views import DrugRAGView

urlpatterns = [
    path("drug/", DrugRAGView.as_view()),  
]
