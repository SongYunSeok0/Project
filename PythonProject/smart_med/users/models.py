from django.db import models
from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin

# ✅ 사용자 정의 UserManager
class UserManager(BaseUserManager):
    def create_user(self, username, password=None, **extra_fields):
        if not username:
            raise ValueError('사용자 이름(username)은 필수입니다.')
        user = self.model(username=username, **extra_fields)
        user.set_password(password)  # 비밀번호 해싱
        user.save(using=self._db)
        return user

    def create_superuser(self, username, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        return self.create_user(username, password, **extra_fields)


# ✅ 사용자 모델
class User(AbstractBaseUser, PermissionsMixin):
    username = models.CharField(max_length=50, unique=True)  # ✅ 아이디 중복 불가
    name = models.CharField(max_length=100)
    birth_date = models.DateField(null=True, blank=True)
    gender = models.CharField(max_length=10, blank=True)
    phone = models.CharField(max_length=20, unique=True)  # ✅ 전화번호 중복 불가
    preferences = models.JSONField(default=dict, blank=True)

    # Django 관리용 필드
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)

    # ✅ username을 로그인 식별자로 사용
    USERNAME_FIELD = 'username'
    REQUIRED_FIELDS = ['phone', 'name']

    objects = UserManager()

    def __str__(self):
        return self.username


class Protector(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='protectors')
    name = models.CharField(max_length=50)
    phone = models.CharField(max_length=20)
    relation = models.CharField(max_length=30, blank=True)

