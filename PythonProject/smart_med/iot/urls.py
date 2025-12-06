from django.urls import path
from . import views
from .views import CommandView, RegisterDeviceView, CreateDeviceView

urlpatterns = [
    path("ingest/", views.ingest, name="iot-ingest"),
    path('alerts/commands/', CommandView.as_view(), name='commands'),
    path("device/register/", RegisterDeviceView.as_view()),
    path("device/create/", CreateDeviceView.as_view()),

]
