from django.urls import path
from . import views
from .views import CommandView, RegisterDeviceView, QRCodeView

urlpatterns = [
    path("ingest/", views.ingest, name="iot-ingest"),
    path('alerts/commands/', CommandView.as_view(), name='commands'),
    path("device/register/", RegisterDeviceView.as_view(), name="device-register"),
    path("device/qr/<str:device_uuid>/", QRCodeView.as_view()),

]
