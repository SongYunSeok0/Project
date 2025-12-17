from django.db import models

from medications.models import RegiHistory


from django.db import models
from django.contrib.postgres.fields import JSONField

class Notification(models.Model):
    regihistory = models.ForeignKey(RegiHistory, on_delete=models.CASCADE)

    notification_type = models.CharField(max_length=30)
    status = models.CharField(
        max_length=20,
        choices=[("SUCCESS", "성공"), ("FAILED", "실패")],
        default="SUCCESS"
    )
    sent_at = models.DateTimeField(null=True, blank=True)
    error_message = models.TextField(null=True, blank=True)

    metadata = models.JSONField(null=True, blank=True)

    created_at = models.DateTimeField(auto_now_add=True)


