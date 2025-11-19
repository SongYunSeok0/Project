package com.data.network.dto.user

data class UserUpdateDto(
    val username: String,
    val height: Double?,
    val weight: Double?,
    val gender: String?,
    val birth_date: String?
)
