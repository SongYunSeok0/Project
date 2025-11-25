import uuid
from django.db import models
from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.core.validators import RegexValidator, MinValueValidator
from django.db.models.functions import Lower
from datetime import date
from django.core.exceptions import ValidationError


class UserManager(BaseUserManager):
    def create_user(self, email=None, password=None, **extra):
        # 1124 소셜로그인 적용으로 이메일/비번 필수 로직 수정, 메시지는 UserCreateSerializer로 이동
        # 소셜 유저는 이메일 없이 계정 생성 (프로바이더+소셜아이디로 구분)
        if not email:
            email = None
        else:
            email = self.normalize_email(email)

        user = self.model(email=email, **extra)

        # 비번 있으면 로컬 유저 -> 비번 설정 루트, 비번 없으면 소셜 유저
        if password:
            user.set_password(password)
        else:
            user.set_unusable_password()

        user.save(using=self._db)
        return user

    def create_superuser(self, email, password=None, **extra):
        if not password:
            raise ValueError("superuser는 비밀번호가 필요합니다.")
        extra.setdefault("is_staff", True)
        extra.setdefault("is_superuser", True)
        if extra.get("is_staff") is not True:
            raise ValueError("superuser는 is_staff=True 여야 합니다.")
        if extra.get("is_superuser") is not True:
            raise ValueError("superuser는 is_superuser=True 여야 합니다.")
        return self.create_user(email, password, **extra)


def validate_birth_date(value):
    today = date.today()
    if value > today:
        raise ValidationError("미래 날짜는 입력할 수 없습니다.")
    age = today.year - value.year - ((today.month, today.day) < (value.month, value.day))
    if age > 120:
        raise ValidationError("120세를 초과할 수 없습니다.")

class Gender(models.TextChoices):
    MALE = "M", "남"
    FEMALE = "F", "여"


class User(AbstractBaseUser, PermissionsMixin):
    id = models.BigAutoField(primary_key=True)  # 생략 가능. 명시 유지.
    uuid = models.UUIDField(default=uuid.uuid4, unique=True, editable=False)  # 앱 단용 식별자

    firebase_uid = models.CharField(
        max_length=128,
        unique=True,
        null=True,
        blank=True,
        help_text="Firebase phone auth UID"
    )

    # 1121 11:59 소셜로그인용 소셜아이디+프로바이더 추가
    social_id = models.CharField(
        max_length=128,
        unique=True,
        null=True,
        blank=True
    )
    provider = models.CharField(
        max_length=128,
        null=True,
        blank=True
    )
    # 로그인/식별
    # 1121 11:59 소셜로그인 적용을 위해 이메일/유저네임/휴대폰 필드 널허용+빈칸허용으로 수정
    email = models.EmailField(unique=True, db_index=True, null=True, blank=True)
    # password 필드는 AbstractBaseUser에서 상속됨 (CharField, max_length=128)
    username = models.CharField(max_length=100, null=True, blank=True)
    phone = models.CharField(
        max_length=20,
        unique=True,
        validators=[RegexValidator(r"^\+?\d{9,15}$")],
        null=True,
        blank=True
    )

    # 부가 정보
    birth_date = models.DateField(
        null=True, blank=True,
        validators=[validate_birth_date]
    )
    gender = models.CharField(max_length=1, choices=Gender.choices, blank=True, null=True)
    height = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True,
        validators=[MinValueValidator(0)]
    )
    weight = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True,
        validators=[MinValueValidator(0)]
    )
    preferences = models.JSONField(default=dict, blank=True)
    prot_phone = models.CharField(max_length=20, blank=True, null=True)
    relation = models.CharField(max_length=30, blank=True, null=True)

    # 자동 기록
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    # Django 관리용
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)

    fcm_token = models.CharField(
        max_length=512,
        blank=True,
        null=True,
        help_text="마지막으로 등록된 FCM 디바이스 토큰"
    )

    # 인증 설정
    EMAIL_FIELD = "email"
    USERNAME_FIELD = "email"
    # 1121 소셜로그인 적용으로 유저네임,휴대폰번호 제거
    REQUIRED_FIELDS = []  # createsuperuser 시 추가 입력


    objects = UserManager()

    class Meta:
        db_table = "users"
        constraints = [
            # 이메일 대소문자 무시 고유
            models.UniqueConstraint(Lower("email"), name="uniq_user_email_ci"),
        ]
        indexes = [
            models.Index(Lower("email"), name="idx_user_email_lower"),
        ]

    def __str__(self):
        return self.email

class FcmToken(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    token = models.CharField(max_length=300)
    updated_at = models.DateTimeField(auto_now=True)

class EmailVerification(models.Model):
    email = models.EmailField(unique=True)
    code = models.CharField(max_length=6)
    is_verified = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    expire_at = models.DateTimeField()
