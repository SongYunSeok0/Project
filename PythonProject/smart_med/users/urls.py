# users/urls.py
from django.urls import path
from .views import SignupView,LoginView,MeView,RegisterFcmTokenView,SocialLoginView

urlpatterns = [
    path('signup/', SignupView.as_view(), name='signup'),
    path("login/", LoginView.as_view(), name="login"),
    path("social-login/", SocialLoginView.as_view(), name="social-login"),
    path("me/", MeView.as_view(), name="me"),
    path("fcm/", RegisterFcmTokenView.as_view()),
]