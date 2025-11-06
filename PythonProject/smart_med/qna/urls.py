from django.urls import path
from .views import get_answer

urlpatterns = [
    path('ask/', get_answer, name='ask-question'),
]