from django.contrib import admin
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from medications.views import (
    MedicationViewSet,
    MedicationScheduleViewSet,
    MedicationHistoryViewSet,
)


router = DefaultRouter()
router.register(r'medications', MedicationViewSet, basename='medication')
router.register(r'schedules', MedicationScheduleViewSet, basename='schedule')
router.register(r'history', MedicationHistoryViewSet, basename='history')

urlpatterns = [
    path('admin/', admin.site.urls),

    # ✅ 기존 앱들
    path('api/alerts/', include('alerts.urls')),  # POST /api/alerts/sensor/
    path('api/qna/', include('qna.urls')),
    path('api/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('api/ocr/', include('ocr.urls')),
    path('api/users/', include('users.urls')),

    # ✅ 공통 라우터
    path('api/', include(router.urls)),
]
