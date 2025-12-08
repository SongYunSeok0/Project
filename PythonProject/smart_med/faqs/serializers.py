from rest_framework import serializers
from .models import Faq, FaqComment

class FaqCommentSerializer(serializers.ModelSerializer):
    username = serializers.CharField(source='user.username', read_only=True)
    is_staff = serializers.BooleanField(source='user.is_staff', read_only=True)
    
    class Meta:
        model = FaqComment
        fields = [
            'id',
            'faq',
            'user',
            'username',
            'content',
            'created_at',
            'is_staff'
        ]
        read_only_fields = ['id', 'user', 'created_at']


class FaqSerializer(serializers.ModelSerializer):
    username = serializers.CharField(source='user.username', read_only=True)
    comment_count = serializers.SerializerMethodField()
    
    class Meta:
        model = Faq
        fields = [
            'id',
            'user',
            'username',
            'title',
            'content',
            'category',
            'is_answered',
            'created_at',
            'updated_at',
            'comment_count'
        ]
        read_only_fields = ['id', 'user', 'created_at', 'updated_at']
    
    def get_comment_count(self, obj):
        return obj.comments.count()