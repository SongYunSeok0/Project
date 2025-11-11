package com.data.network.dto.user

data class LoginResponse(
    val access: String,
    val refresh: String
)