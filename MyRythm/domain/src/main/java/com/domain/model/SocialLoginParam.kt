package com.domain.model

data class SocialLoginParam(
    val provider: String,
    val socialId: String,
    val accessToken: String? = null,
    val idToken: String? = null,
    val email: String? = null,
    val name: String? = null,
    val profileImageUrl: String? = null
)
