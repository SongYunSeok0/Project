from django.db import models
from django.conf import settings

class Faq(models.Model):
    CATEGORY_CHOICES = [
        ('general', '일반 문의'),
        ('account', '계정 관련'),
        ('medication', '복약 관련'),
        ('device', '기기 관련'),
        ('other', '기타'),
    ]
    
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='faqs'
    )
    title = models.CharField(max_length=200)
    content = models.TextField()
    category = models.CharField(
        max_length=20,
        choices=CATEGORY_CHOICES,
        default='general'
    )
    is_answered = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'faqs'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.title} - {self.user.username}"


class FaqComment(models.Model):
    faq = models.ForeignKey(
        Faq,
        on_delete=models.CASCADE,
        related_name='comments'
    )
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='faq_comments'
    )
    content = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'faq_comments'
        ordering = ['created_at']
    
    def __str__(self):
        return f"Comment by {self.user.username} on {self.faq.title}"