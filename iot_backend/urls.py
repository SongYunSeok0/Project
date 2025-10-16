from django.contrib import admin
from django.urls import path
from api.views import sensor_data, latest_data

urlpatterns = [
    path('admin/', admin.site.urls),
    path('sensor/', sensor_data),
    path('latest/', latest_data),
]
