# users/services.py

import secrets
from django.conf import settings
from django.core.mail import send_mail
from django.core.cache import cache
from django.contrib.auth import get_user_model
from django.db import transaction

from notifications.services import send_fcm_to_token

User = get_user_model()


# ============================================================
# 1. 로그인/소셜 로그인 관련 서비스
# ============================================================

def get_user_by_login_id(login_id):
    """아이디/이메일로 유저 찾기"""
    return (
            User.objects.filter(username=login_id).first()
            or User.objects.filter(email=login_id).first()
    )


def set_login_cache(user_id):
    """로그인 직후 캐시 설정 (FCM 알림용)"""
    cache_key = f"just_logged_in:{user_id}"
    cache.set(cache_key, True, timeout=60)


def _generate_unique_username(base):
    """중복되지 않는 유저네임 생성 (내부 함수)"""
    username = base
    count = 1
    while User.objects.filter(username=username).exists():
        username = f"{base}_{count}"
        count += 1
    return username


@transaction.atomic
def social_login_get_or_create(provider, social_id):
    """
    소셜 유저 조회 또는 생성
    Return: (user, is_created)
    """
    # 1. 기존 유저 조회
    user = User.objects.filter(provider=provider, social_id=social_id).first()

    if user:
        return user, False  # 기존 유저

    # 2. 신규 유저 생성
    base_username = f"{provider}_{social_id}"
    new_username = _generate_unique_username(base_username)

    user = User.objects.create_user(
        username=new_username,
        provider=provider,
        social_id=social_id,
        email=None,
        password=None,
    )
    return user, True  # 신규 유저


# ============================================================
# 2. 이메일 인증 서비스
# ============================================================

def send_verification_email(email, name=None):
    """인증코드 생성 및 이메일 발송"""
    # 보호자 이름 체크 로직
    if name:
        if not User.objects.filter(email=email, username=name).exists():
            raise ValueError("해당 사용자를 찾을 수 없습니다.")

    # 코드 생성 및 캐싱
    code = secrets.randbelow(900000) + 100000
    cache.set(f"email_code:{email}", code, timeout=180)  # 3분

    # 메일 발송
    send_mail(
        "[MyRhythm] 이메일 인증코드",
        f"인증코드: {code}\n3분 안에 입력해주세요.",
        settings.EMAIL_HOST_USER,
        [email],
    )


def verify_email_code(email, code):
    """이메일 코드 검증"""
    saved_code = cache.get(f"email_code:{email}")

    if saved_code is None:
        return False, "코드 없음 또는 만료"

    if str(saved_code) != str(code):
        return False, "코드 불일치"

    # 인증 성공 처리
    cache.set(f"email_verified:{email}", True, timeout=300)  # 5분간 유효
    return True, "인증 성공"


def check_email_verified(email):
    """회원가입 전 이메일 인증 여부 확인"""
    if not cache.get(f"email_verified:{email}"):
        return False
    return True


def clear_email_cache(email):
    """회원가입 완료 후 캐시 정리"""
    cache.delete(f"email_verified:{email}")
    cache.delete(f"email_code:{email}")


# ============================================================
# 3. FCM 알림 서비스
# ============================================================

def register_fcm_and_notify(user, token):
    """FCM 토큰 저장 및 로그인 알림 발송"""
    # 토큰 업데이트
    user.fcm_token = token
    user.save(update_fields=["fcm_token"])

    # 방금 로그인했는지 확인
    cache_key = f"just_logged_in:{user.id}"
    is_just_logged_in = cache.get(cache_key)

    if is_just_logged_in:
        try:
            send_fcm_to_token(
                token=token,
                title="로그인 알림",
                body=f"{user.username} 님이 로그인했습니다.",
            )
        except Exception as e:
            # 로그는 남기되, 에러를 뷰로 전파하진 않음
            print(f"FCM Send Error: {e}")

        # 알림 보냈으니 캐시 삭제
        cache.delete(cache_key)