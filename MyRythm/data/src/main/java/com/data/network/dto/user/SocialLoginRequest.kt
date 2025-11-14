package com.data.network.dto.user

data class SocialLoginRequest(
    val socialId: String,
    val provider: String,
    val accessToken: String? = null,
    val idToken: String? = null,

    val email: String? = null,
    val name: String? = null,
    val profileImageUrl: String? = null
)