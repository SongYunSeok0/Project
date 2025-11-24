package com.domain.model

data class SignupRequest(
    val email: String? = null,
    val username: String? = null,
    val phone: String,
    val birthDate: String,
    val gender: String,
    val height: Double,
    val weight: Double,
    val password: String? = null,
    val provider: String? = null,
    val socialId: String? = null
)
//1124일부수정
