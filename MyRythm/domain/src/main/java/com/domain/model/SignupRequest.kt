package com.domain.model

import com.squareup.moshi.Json

data class SignupRequest(
    val email: String,
    val username: String,
    val password: String,
    val phone: String,
    @Json(name = "birth_date") val birthDate: String, // yyyy-MM-dd
    val gender: String,   // 서버 choices에 맞게: 예) "male"/"female"/"unknown" 또는 "M"/"F"/"U"
    val height: Double,
    val weight: Double
)
