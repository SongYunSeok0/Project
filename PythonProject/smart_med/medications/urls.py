from django.urls import path
from .views import PlanListView
from .views import regihistoryListCreateView   # 🔥 방금 만든 regihistory View

urlpatterns = [
    path("regihistory/", regihistoryListCreateView.as_view(), name="regihistory"),
    path("plan/", PlanListView.as_view(), name="plan_list"),
]
