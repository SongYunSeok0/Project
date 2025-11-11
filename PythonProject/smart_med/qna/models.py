from django.db import models

class QnA(models.Model):
    question = models.TextField()
    answer = models.TextField()
    category = models.CharField(max_length=100, blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.question[:50]
