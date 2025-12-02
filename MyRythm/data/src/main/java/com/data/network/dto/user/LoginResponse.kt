package com.data.network.dto.user

data class LoginResponse(
    val access: String,
    val refresh: String,
    val user_id: Long?, //1127 18:08
    )