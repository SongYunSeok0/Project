package com.data.mapper.user

import com.data.db.entity.UserEntity
import com.data.network.dto.user.UserDto
import com.data.network.dto.user.UserUpdateDto
import com.domain.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.domain.model.UserProfile
import java.time.LocalDate
import kotlin.String

private val moshi = Moshi.Builder().build()
private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
private val mapAdapter = moshi.adapter<Map<String, Any>>(mapType)

/** Remote DTO → Local Entity */
fun UserDto.asEntity(): UserEntity = UserEntity(
    id = id,
    uuid = uuid,
    email = email,
    username = username,
    phone = phone,
    birthDate = birth_date,
    gender = gender,
    height = height,
    weight = weight,
    preferences = mapAdapter.toJson(preferences ?: emptyMap()),
    protPhone = prot_phone,
    relation = relation,
    isActive = is_active,
    isStaff = is_staff,
    createdAt = created_at,
    updatedAt = updated_at,
    lastLogin = last_login
)

/** Local Entity → Domain */
fun UserEntity.asDomain(): User = User(
    id = id,
    uuid = uuid,
    email = email,
    username = username,
    phone = phone,
    birthDate = birthDate,
    gender = gender,
    height = height,
    weight = weight,
    preferences = preferences?.let { mapAdapter.fromJson(it) } ?: emptyMap(),
    protPhone = protPhone,
    relation = relation,
    isActive = isActive,
    isStaff = isStaff,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastLogin = lastLogin
)

fun UserDto.toProfile(): UserProfile {
    val age = birth_date?.let { birth ->
        try {
            val year = birth.substring(0, 4).toInt()
            val now = java.time.LocalDate.now().year
            now - year
        } catch (_: Exception) {
            null
        }
    }

    return UserProfile(
        username = username,
        height = height,
        weight = weight,
        age = age,
        birth_date = birth_date,
        gender = gender,
        phone = phone,
        prot_phone = prot_phone,
        email = email,
    )
}

fun UserProfile.toDto(): UserUpdateDto {
    return UserUpdateDto(
        username = username?: "",
        height = height,
        weight = weight,
        gender = gender,
        birth_date =birth_date,
        phone = phone,
        prot_phone = prot_phone,
        email = email?: "",
    )
}

