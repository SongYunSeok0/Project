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
            "prot_email",
            "relation",
            "provider",
            "social_id",
            "is_active",
            "is_staff",
            "created_at",
            "updated_at",
            "last_login",
        )


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

        # 소셜 계정인 경우 create_user 호출 방식 변경
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


class UserUpdateSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, required=False, min_length=8)

    # [수정 1] prot_name은 실제 모델 필드가 아니므로 명시적으로 선언해야 값을 받을 수 있습니다.
    prot_name = serializers.CharField(write_only=True, required=False, allow_blank=True)

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
            "prot_email",
            "relation",
            "email",
            "prot_name"  # [수정 2] 주석 해제 및 필드 추가
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
        # 1. 패스워드와 보호자 이름(prot_name)을 데이터에서 꺼냄
        pwd = validated_data.pop("password", None)

        # [수정] 이제 fields에 등록되었으므로 validated_data에 값이 들어옵니다.
        prot_name_input = validated_data.pop("prot_name", None)

        # 2. [핵심] prot_name이 들어왔다면 DB의 'relation' 필드에 저장
        if prot_name_input:
            instance.relation = prot_name_input

        # 3. 나머지 데이터 업데이트
        for k, v in validated_data.items():
            setattr(instance, k, v)

        # 4. 비밀번호 변경 시 처리
        if pwd:
            instance.set_password(pwd)

        try:
            instance.save()
        except IntegrityError as e:
            if "phone" in str(e).lower():
                raise serializers.ValidationError({"phone": "이미 사용 중인 전화번호입니다."})
            raise
        return instance