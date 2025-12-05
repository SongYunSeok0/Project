package com.data.network.dto.user

data class SendCodeRequest(
    val email: String,
    val name: String? = null
)