from django.contrib import admin
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from medications.views import (PlanListView
)

from users.views import SendEmailCodeView, VerifyEmailCodeView



router = DefaultRouter()


urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/iot/', include('iot.urls')),
    path('api/users/', include('users.urls')),
    path('api/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('api/', include(router.urls)),
     path("api/auth/send-code/", SendEmailCodeView.as_view()),
    path("api/auth/verify-code/", VerifyEmailCodeView.as_view()),
    path('api/plan/', PlanListView.as_view(), name='plan_list'),
    path('api/rag/', include('rag.urls')),
    path('api/health/', include('health.urls')),
]
