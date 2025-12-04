from django.contrib import admin
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenRefreshView

from medications.plan_test_view import test_med_alarm_view
from medications.views import (PlanListView
)
from users.views import CustomTokenObtainPairView
from users.views import SendEmailCodeView, VerifyEmailCodeView

from drf_spectacular.views import (
    SpectacularAPIView,
    SpectacularSwaggerView,
    SpectacularRedocView,
)


router = DefaultRouter()

urlpatterns = [

    path("api/schema/", SpectacularAPIView.as_view(), name="schema"),
    path(
        "api/docs/",SpectacularSwaggerView.as_view(
            url_name="schema",
            authentication_classes=[],
            permission_classes=[],
        ),
    ),
    path("api/redoc/", SpectacularRedocView.as_view(url_name="schema"), name="redoc"),

    path('admin/', admin.site.urls),
    path('api/iot/', include('iot.urls')),
    path('api/users/', include('users.urls')),
    path('api/token/', CustomTokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('api/', include(router.urls)),
    path("api/auth/send-code/", SendEmailCodeView.as_view()),
    path("api/auth/verify-code/", VerifyEmailCodeView.as_view()),
    path('api/plan/', PlanListView.as_view(), name='plan_list'),
    path('api/rag/', include('rag.urls')),
    path('api/health/', include('health.urls')),
    path('test-alarm/', test_med_alarm_view, name='test_alarm'),
]
