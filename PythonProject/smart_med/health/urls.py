from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import HeartRateViewSet, StepCountViewSet

router = DefaultRouter()
router.register(r'heart', HeartRateViewSet)
router.register(r'steps', StepCountViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
