from django.contrib import admin
from .models import Faq, FaqComment

@admin.register(Faq)
class FaqAdmin(admin.ModelAdmin):
    list_display = ['id', 'title', 'user', 'category', 'is_answered', 'created_at']
    list_filter = ['category', 'is_answered', 'created_at']
    search_fields = ['title', 'content', 'user__username']
    readonly_fields = ['created_at', 'updated_at']

@admin.register(FaqComment)
class FaqCommentAdmin(admin.ModelAdmin):
    list_display = ['id', 'faq', 'user', 'created_at']
    list_filter = ['created_at']
    search_fields = ['content', 'user__username']
    readonly_fields = ['created_at']