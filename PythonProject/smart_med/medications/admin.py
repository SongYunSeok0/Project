from django.contrib import admin
from .models import Medication, MedicationSchedule, MedicationHistory

admin.site.register(Medication)
admin.site.register(MedicationSchedule)
admin.site.register(MedicationHistory)
