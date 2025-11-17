from django.contrib import admin
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from medications.views import (PlanListView
)

from health.views import HeartRateViewSet, StepCountViewSet


router = DefaultRouter()

# ✅ health (심박수 / 걸음수)
router.register(r'health/heart', HeartRateViewSet, basename='heart')
router.register(r'health/steps', StepCountViewSet, basename='steps')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/iot/', include('iot.urls')),
    path('api/users/', include('users.urls')),
    path('api/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('api/', include(router.urls)),
    path('api/rag/', include('rag.urls')),
    path('api/plans/', PlanListView.as_view(), name='plan_list'),
]
