package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Long,
    val uuid: String,
    val email: String,
    val username: String,
    val phone: String?,
    val birthDate: String?,
    val gender: String?,
    val height: Double?,
    val weight: Double?,
    val preferences: String?,
    val protPhone: String?,
    val relation: String?,
    val isActive: Boolean,
    val isStaff: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLogin: String?
)
