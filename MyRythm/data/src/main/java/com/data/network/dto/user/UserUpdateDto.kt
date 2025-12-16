package com.data.network.dto.user

data class UserUpdateDto(
    val username: String,
    val height: Double?,
    val weight: Double?,
    val gender: String? = null,
    val birth_date: String?,
    val phone: String?,
    val prot_name: String?,
    val relation: String?,
    val prot_email: String?,
    val email: String,
)