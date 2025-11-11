# users/serializers.py
from django.db import IntegrityError
from rest_framework import serializers
from .models import User, Gender


def _normalize_phone(s: str) -> str:
    return "".join(ch for ch in (s or "") if ch.isdigit() or ch == "+")


class UserCreateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)

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
        return user


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
