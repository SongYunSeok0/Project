from django.urls import path
from .views import (
    RegiHistoryListCreateView,
    RegiHistoryUpdateView,
    RegiHistoryDeleteView,
    PlanListView,
    PlanUpdateView,
    PlanDeleteView,
    TodayPlansView,
)

urlpatterns = [
    # RegiHistory
    path("regihistory/", RegiHistoryListCreateView.as_view(), name="regihistory_list_create"),
    path("regihistory/<int:pk>/", RegiHistoryUpdateView.as_view(), name="regihistory_update"),
    path("regihistory/<int:pk>/delete/", RegiHistoryDeleteView.as_view(), name="regihistory_delete"),

    # Plan
    path("plan/", PlanListView.as_view(), name="plan_list_create"),
    path("plan/<int:pk>/", PlanUpdateView.as_view(), name="plan_update"),
    path("plan/<int:pk>/delete/", PlanDeleteView.as_view(), name="plan_delete"),

    # Today plans
    path("plan/today/", TodayPlansView.as_view(), name="today_plans"),
]