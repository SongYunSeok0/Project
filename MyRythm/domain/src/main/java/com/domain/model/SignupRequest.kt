package com.domain.model

data class SignupRequest(
    val email: String,
    val username: String,
    val phone: String,
    val birthDate: String,   // "yyyy-MM-dd" 등 서버 포맷
    val gender: String,      // "M" | "F" | "OTHER" 등 서버 규칙
    val height: Double,
    val weight: Double,
    val password: String
)