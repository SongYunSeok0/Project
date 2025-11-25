# users/urls.py
from django.urls import path
from .views import SignupView,LoginView,MeView,RegisterFcmTokenView,WithdrawalView

urlpatterns = [
    path('signup/', SignupView.as_view(), name='signup'),
    path("login/", LoginView.as_view(), name="login"),
    # path("login/social-login", SocialLoginView.as_view(), name="sociallogin"),
    path("me/", MeView.as_view(), name="me"),
    path("fcm/", RegisterFcmTokenView.as_view()),
    path('withdrawal/', WithdrawalView.as_view(), name='withdrawal'),
]
