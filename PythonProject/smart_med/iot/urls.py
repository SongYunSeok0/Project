from django.urls import path
from . import views

urlpatterns = [
    path("ingest/", views.ingest, name="iot-ingest"),
    # path('commands/', views.get_command, name='get-command'),
]
