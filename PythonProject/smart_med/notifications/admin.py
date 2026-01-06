from django.contrib import admin
from .models import Notification


# 이렇게 등록하면 관리자 페이지에서 엑셀처럼 표로 보입니다.
@admin.register(Notification)
class NotificationAdmin(admin.ModelAdmin):
    # 목록에 보여줄 컬럼들 (SQL 조회 결과랑 비슷하게 설정)
    list_display = ('id', 'notification_type', 'status', 'regihistory_id', 'sent_at')

    # 우측에 필터 기능 추가 (성공/실패 여부 등으로 거르기 편함)
    list_filter = ('status', 'notification_type')

    # 검색 기능 추가 (메타데이터 내용으로 검색 가능)
    search_fields = ('metadata', 'error_message')