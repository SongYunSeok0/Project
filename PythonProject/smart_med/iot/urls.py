from django.urls import path
from . import views
from .views import CommandView, RegisterDeviceView

urlpatterns = [
    path("ingest/", views.ingest, name="iot-ingest"),
    path('alerts/commands/', CommandView.as_view(), name='commands'),
    path("device/register/", RegisterDeviceView.as_view(), name="device-register"),
]
