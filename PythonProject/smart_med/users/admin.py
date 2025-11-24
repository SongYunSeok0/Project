from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import User

@admin.register(User)
class UserAdmin(BaseUserAdmin):
    model = User
    list_display = ("email","username","phone","is_staff","is_active")
    list_filter  = ("is_staff","is_active","gender","is_superuser","groups")
    search_fields = ("email","username","phone")
    ordering = ("-id",)

    fieldsets = (
        (None, {"fields": ("email","password")}),
        ("Personal info", {
            "fields": ("username","phone","birth_date","gender",
                       "height","weight","preferences","prot_email","relation","uuid")
        }),
        ("Permissions", {"fields": ("is_active","is_staff","is_superuser","groups","user_permissions")}),
        ("Important dates", {"fields": ("last_login","created_at","updated_at")}),
    )

    add_fieldsets = (
        (None, {
            "classes": ("wide",),
            "fields": ("email","password1","password2","username","phone","is_staff","is_active"),
        }),
    )

    readonly_fields = ("uuid","created_at","updated_at","last_login")
    filter_horizontal = ("groups","user_permissions")
