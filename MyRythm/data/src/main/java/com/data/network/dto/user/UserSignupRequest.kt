package com.data.network.dto.user

data class UserSignupRequest(
    val email: String,
    val username: String,
    val phone: String,
    val birthDate: String,
    val gender: String,
    val height: Double,
    val weight: Double,
    val password: String
)
