from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import HeartRateViewSet, DailyStepViewSet

router = DefaultRouter()
router.register(r'heart', HeartRateViewSet)
router.register(r'dailystep', DailyStepViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
