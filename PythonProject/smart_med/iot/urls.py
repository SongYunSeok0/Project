from django.urls import path
from . import views
from .views import CommandView, RegisterDeviceView, QRCodeView,MyDeviceListView

urlpatterns = [
    path("ingest/", views.ingest, name="iot-ingest"),
    path("devices/", MyDeviceListView.as_view(), name="device"),
    path('alerts/commands/', CommandView.as_view(), name='commands'),
    path("device/register/", RegisterDeviceView.as_view()),
    path("device/qr/<str:device_uuid>/", QRCodeView.as_view()),
]
