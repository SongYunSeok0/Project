package com.data.network.dto.user

import com.squareup.moshi.Json

data class UserSignupRequest(
    @Json(name = "email") val email: String,
    @Json(name = "username") val username: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "birth_date") val birthDate: String, // 서버와 동일하게
    @Json(name = "gender") val gender: String,        // "M" / "F"
    @Json(name = "height") val height: Double,
    @Json(name = "weight") val weight: Double,
    @Json(name = "password") val password: String

    // sns연동로그인 부분
    val provider: String? = null,   // ✅ kakao / google 등
    val socialId: String? = null    // ✅ SNS 유저 고유 PK
)