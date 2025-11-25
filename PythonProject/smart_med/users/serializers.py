# users/serializers.py
from django.db import IntegrityError
from rest_framework import serializers
from .models import User, Gender


def _normalize_phone(s: str) -> str:
    return "".join(ch for ch in (s or "") if ch.isdigit() or ch == "+")


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = (
            "id",
            "uuid",
            "email",
            "username",
            "phone",
            "birth_date",
            "gender",
            "height",
            "weight",
            "preferences",
            "prot_phone",
            "relation",
            "is_active",
            "is_staff",
            "created_at",
            "updated_at",
            "last_login",
        )

# 1124소셜로그인용수정중
class UserCreateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(
        write_only=True,
        required=False,
        allow_blank=True,
        min_length=8
    )
    email = serializers.EmailField(
        required=False,
        allow_blank=True
    )
    username = serializers.CharField(
        required=False,
        allow_blank=True
    )

    provider = serializers.CharField(required=False, allow_null=True, allow_blank=True)
    socialId = serializers.CharField(required=False, allow_null=True, allow_blank=True)

    class Meta:
        model = User
        fields = (
            "email",
            "password",
            "username",
            "phone",
            "birth_date",
            "gender",
            "preferences",
            "height",
            "weight",
            "provider",
            "socialId",
        )

    def validate(self, attrs):
        provider = attrs.get("provider")
        social_id = attrs.get("socialId")

        # 소셜 회원가입일 경우 필수값 검사 생략
        if provider and social_id:
            return attrs

        # 로컬 회원가입은 기존 필수 검증 실행
        if not attrs.get("email"):
            raise serializers.ValidationError({"email": "이메일은 필수입니다."})
        if not attrs.get("password"):
            raise serializers.ValidationError({"password": "비밀번호는 필수입니다."})
        if not attrs.get("username"):
            raise serializers.ValidationError({"username": "이름은 필수입니다."})

        return attrs

    def create(self, validated_data):
        provider = validated_data.get("provider")
        social_id = validated_data.get("socialId")

        # 소셜 계정인 경우 create_user 호출 방식 변경 (이메일과 비번 필드X 프로바이더와 소셜아이디로 구분O)
        if provider and social_id:
            validated_data.pop("password", None)
            email = validated_data.pop("email", "") or None
            username = validated_data.pop("username", f"{provider}_{social_id}")

            user = User.objects.create(
                email=email,
                username=username,
                provider=provider,
                social_id=social_id,
                **validated_data
            )
            return user

        # 로컬 유저 회원가입
        email = validated_data.pop("email").lower()
        pwd = validated_data.pop("password")
        user = User.objects.create_user(
            email=email,
            password=pwd,
            **validated_data
        )
        return user

"""class UserCreateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(
        write_only=True,
        required=True,
        min_length=8,
        error_messages={
            "required": "비밀번호는 필수입니다.",
            "blank": "비밀번호는 필수입니다."
        }
    )
    email = serializers.EmailField(
        required=True,
        error_messages={
            "required": "이메일은 필수입니다.",
            "blank": "이메일은 필수입니다."
            # "invalid": "유효한 이메일 형식이 아닙니다."
        }
    )


    class Meta:
        model = User
        fields = (
            "email",
            "password",
            "username",
            "phone",
            "birth_date",
            "gender",
            "preferences",
            "height",
            "weight",
        )

    def validate_email(self, v: str) -> str:
        return (v or "").strip().lower()

    def validate_phone(self, v: str) -> str:
        return _normalize_phone(v)

    def validate_gender(self, v: str) -> str | None:
        if v in (None, ""):
            return None
        m = {
            "m": "M", "male": "M", "남": "M", "남자": "M",
            "f": "F", "female": "F", "여": "F", "여자": "F",
        }
        v2 = m.get(str(v).strip().lower(), v)
        if v2 not in dict(Gender.choices):
            raise serializers.ValidationError("gender는 M 또는 F 중 하나여야 합니다.")
        return v2

    def create(self, validated_data):
        email = validated_data.pop("email").lower()
        pwd = validated_data.pop("password")
        user = User.objects.create_user(
            email=email,
            password=pwd,
            **validated_data
        )
        return user"""


class UserUpdateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, required=False, min_length=8)

    class Meta:
        model = User
        fields = (
            "username",
            "phone",
            "password",
            "preferences",
            "birth_date",
            "gender",
            "height",
            "weight",
            "prot_phone",
        )

    def validate_phone(self, v: str) -> str:
        return _normalize_phone(v)

    def validate_gender(self, v: str) -> str | None:
        if v in (None, ""):
            return None
        m = {
            "m": "M", "male": "M", "남": "M", "남자": "M",
            "f": "F", "female": "F", "여": "F", "여자": "F",
        }
        v2 = m.get(str(v).strip().lower(), v)
        if v2 not in dict(Gender.choices):
            raise serializers.ValidationError("gender는 M 또는 F 중 하나여야 합니다.")
        return v2

    def update(self, instance, validated_data):
        pwd = validated_data.pop("password", None)

        for k, v in validated_data.items():
            setattr(instance, k, v)

        if pwd:
            instance.set_password(pwd)

        try:
            instance.save()
        except IntegrityError as e:
            if "phone" in str(e).lower():
                raise serializers.ValidationError({"phone": "이미 사용 중인 전화번호입니다."})
            raise
        return instance
