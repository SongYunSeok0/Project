package com.domain.model

data class SignupRequest(
    val email: String,
    val username: String,
    val phone: String,
    val birthDate: String,
    val gender: String,
    val height: Double,
    val weight: Double,
    val password: String
)
