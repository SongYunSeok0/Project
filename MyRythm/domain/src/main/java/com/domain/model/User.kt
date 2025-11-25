package com.domain.model

data class User(
    val id: Long,
    val uuid: String?,
    val email: String?,
    val username: String,
    val phone: String?,
    val birthDate: String?,
    val gender: String?,
    val height: Double?,
    val weight: Double?,
    val preferences: Map<String, Any>?,
    val protPhone: String?,
    val relation: String?,
    val isActive: Boolean,
    val isStaff: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLogin: String?
)
