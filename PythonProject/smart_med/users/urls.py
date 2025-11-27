# users/urls.py
from django.urls import path
from .views import SignupView, MeView, RegisterFcmTokenView, WithdrawalView, CustomTokenObtainPairView

urlpatterns = [
    path('signup/', SignupView.as_view(), name='signup'),
    # path("login/social-login", SocialLoginView.as_view(), name="sociallogin"),
    path("me/", MeView.as_view(), name="me"),
    path("fcm/", RegisterFcmTokenView.as_view()),
    path('withdrawal/', WithdrawalView.as_view(), name='withdrawal'),
]
