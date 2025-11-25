from django.urls import path
from .views import PlanListView
from .views import RegiHistoryListCreateView   # 🔥 방금 만든 RegiHistory View

urlpatterns = [
    path("regihistory/", RegiHistoryListCreateView.as_view(), name="regihistory"),
    path("plan/", PlanListView.as_view(), name="plan_list"),
]
