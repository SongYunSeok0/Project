package com.data.network.dto.user

data class UserUpdateDto(
    val username: String,
    val height: Double?,
    val weight: Double?,
    val gender: String? = null,
    val birth_date: String?,
    val phone: String?,
    val prot_phone: String?,
    val email: String,
)
