package com.data.network.dto.user

data class VerifyCodeRequest(
    val email: String,
    val code: String
)