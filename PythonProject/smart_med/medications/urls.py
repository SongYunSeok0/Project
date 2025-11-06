from rest_framework.routers import DefaultRouter
from .views import MedicationViewSet, MedicationScheduleViewSet, MedicationHistoryViewSet

router = DefaultRouter()
router.register(r'medications', MedicationViewSet, basename='medication')
router.register(r'schedules', MedicationScheduleViewSet, basename='schedule')
router.register(r'history', MedicationHistoryViewSet, basename='history')

urlpatterns = router.urls
