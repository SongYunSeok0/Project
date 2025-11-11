package com.data.network.dto.user

data class UserLoginRequest(
    val email: String,
    val password: String
)