# users/serializers.py

from django.contrib.auth import get_user_model, authenticate
from django.db import IntegrityError
from rest_framework import serializers
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

from .models import Gender

User = get_user_model()

# 관리자 이메일 목록 (하드코딩보다는 환경변수나 DB 관리가 좋지만, 일단 유지)
STAFF_EMAILS = {
    'qkfrus6623@naver.com'
}


class UserSerializer(serializers.ModelSerializer):
    """
    사용자 정보 조회용 Serializer
    """

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
    """
    회원가입(로컬/소셜 추가 정보 입력)용 Serializer
    """
    password = serializers.CharField(
        write_only=True, required=False, allow_blank=True, min_length=8
    )
    email = serializers.EmailField(required=False, allow_blank=True)
    username = serializers.CharField(required=False, allow_blank=True)

    # CamelCase(프론트) -> SnakeCase(DB) 매핑을 위해 필드명 유지
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

        # 1. 소셜 로그인 연동 가입인 경우 -> 필수값 체크 완화
        if provider and social_id:
            return attrs

        # 2. 일반 로컬 회원가입인 경우 -> 필수값 체크
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

        # 1. 데이터 추출 및 정리 (딕셔너리에서 제거)
        email_input = validated_data.pop("email", "")
        email = email_input.lower() if email_input else ""
        password = validated_data.pop("password", None)

        # 관리자 여부
        is_staff = (email in STAFF_EMAILS)

        # 2. 소셜 가입
        if provider and social_id:
            username = validated_data.pop("username", None) or f"{provider}_{social_id}"

            return User.objects.create(
                username=username,
                email=email or None,
                is_staff=is_staff,
                provider=provider,
                social_id=social_id,
                **validated_data
            )

        # 3. 로컬 가입 (create_user 사용)
        return User.objects.create_user(
            email=email,
            password=password,
            is_staff=is_staff,
            **validated_data
        )


class UserUpdateSerializer(serializers.ModelSerializer):
    """
    회원 정보 수정용 Serializer
    """
    password = serializers.CharField(write_only=True, required=False, min_length=8)

    # [설명] prot_name은 DB에 없는 필드지만, 입력받아서 relation 필드에 넣기 위해 선언
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
            "prot_name",
        )

    def validate_phone(self, value):
        """전화번호 숫자만 남기기"""
        return "".join(filter(str.isdigit, value)) if value else ""

    def validate_gender(self, value):
        """성별 입력값 정규화 (M/F)"""
        if not value:
            return None

        normalized = value.strip().lower()
        map_gender = {
            "m": "M", "male": "M", "남": "M", "남자": "M",
            "f": "F", "female": "F", "여": "F", "여자": "F",
        }

        result = map_gender.get(normalized, value)
        if result not in dict(Gender.choices):
            raise serializers.ValidationError("gender는 M 또는 F 중 하나여야 합니다.")

        return result

    def update(self, instance, validated_data):
        password = validated_data.pop("password", None)
        prot_name = validated_data.pop("prot_name", None)

        # 1. prot_name -> relation 필드로 매핑
        if prot_name:
            instance.relation = prot_name

        # 2. 나머지 필드 자동 업데이트
        for attr, value in validated_data.items():
            setattr(instance, attr, value)

        # 3. 비밀번호 변경 시 암호화
        if password:
            instance.set_password(password)

        try:
            instance.save()
        except IntegrityError as e:
            if "phone" in str(e).lower():
                raise serializers.ValidationError({"phone": "이미 사용 중인 전화번호입니다."})
            raise

        return instance


class CustomTokenObtainPairSerializer(TokenObtainPairSerializer):
    """
    이메일 + 비밀번호 로그인 전용 Serializer
    """
    username_field = 'email'

    def validate(self, attrs):
        # 1. 이메일/비번 입력 확인
        email = attrs.get("email")
        password = attrs.get("password")

        if not email or not password:
            raise serializers.ValidationError("이메일과 비밀번호를 모두 입력해야 합니다.")

        # 2. 인증 시도
        # request context를 전달해야 authenticate가 내부 로깅 등을 처리할 수 있음
        user = authenticate(
            request=self.context.get('request'),
            email=email,
            password=password
        )

        if not user:
            raise serializers.ValidationError("이메일 또는 비밀번호가 잘못되었습니다.")

        # 3. 토큰 생성
        refresh = self.get_token(user)

        return {
            'refresh': str(refresh),
            'access': str(refresh.access_token),
        }