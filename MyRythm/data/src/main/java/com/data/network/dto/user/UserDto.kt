package com.data.network.dto.user

data class UserDto(
    val id: Long,
    val uuid: String,
    val email: String,
    val username: String,
    val phone: String?,
    val birth_date: String?,
    val gender: String?,
    val height: Double?,
    val weight: Double?,
    val preferences: Map<String, Any>?,
    val prot_email: String?,
    val relation: String?,
    val is_active: Boolean,
    val is_staff: Boolean,
    val created_at: String,
    val updated_at: String,
    val last_login: String?
)
