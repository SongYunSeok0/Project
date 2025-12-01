package com.data.network.dto.user

// 1201
data class ResetPasswordRequest(
    val email: String,
    val password: String
)