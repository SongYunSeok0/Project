package com.auth.data.model

// 카카오, 구글용 데이터클래스 생성. UserApi.kt 파일에서 소셜로그인용 추가해둠
// 서버로 카카오/구글 로그인 시도 하는 거 요청하는 데이터모델 생성
data class SocialLoginRequest(
    val socialId: String,     // 카카오 user.id
    val provider: String,     // "kakao"
    val accessToken: String,   // 카카오 access token

    // 여긴 구글용
    val email: String? = null,
    val name: String? = null,
    val profileImageUrl: String? = null
)