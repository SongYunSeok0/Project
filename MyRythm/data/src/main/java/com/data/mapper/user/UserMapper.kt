package com.data.mapper.user

import com.data.db.entity.UserEntity
import com.data.network.dto.user.UserDto
import com.data.network.dto.user.UserUpdateDto
import com.domain.model.User
import com.domain.model.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.LocalDate

private val moshi = Moshi.Builder().build()
private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
private val mapAdapter = moshi.adapter<Map<String, Any>>(mapType)

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
    protPhone = prot_email,
    relation = relation,
    isActive = is_active,
    isStaff = is_staff,
    createdAt = created_at,
    updatedAt = updated_at,
    lastLogin = last_login
)

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

fun UserDto.toDomain(): User = User(
    id = id,
    uuid = uuid,
    email = email,
    username = username,
    phone = phone,
    birthDate = birth_date,
    gender = gender,
    height = height,
    weight = weight,
    preferences = preferences ?: emptyMap(),
    protPhone = prot_email,
    relation = relation,
    isActive = is_active,
    isStaff = is_staff,
    createdAt = created_at,
    updatedAt = updated_at,
    lastLogin = last_login
)

fun User.toUpdateDto(): UserUpdateDto {
    return UserUpdateDto(
        username = username,
        height = height,
        weight = weight,
        phone = phone,
        prot_email = protPhone,
        prot_name = relation,
        relation = relation
    )
}

fun UserDto.toProfile(): UserProfile {
    val age = birth_date?.let { birth ->
        try {
            val year = birth.substring(0, 4).toInt()
            val now = LocalDate.now().year
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
        prot_name = relation,
        prot_email = prot_email,
        email = email,
        isStaff = is_staff
    )
}

// ✅ 수정: 변경 불가능한 필드 제거
fun UserProfile.toUpdateDto(): UserUpdateDto {
    return UserUpdateDto(
        username = username,
        height = height,
        weight = weight,
        phone = phone,
        prot_email = prot_email,
        prot_name = prot_name,
        relation = prot_name
    )
}

fun UserEntity.toProfile(): UserProfile {
    val age = birthDate?.let { birth ->
        try {
            val year = birth.substring(0, 4).toInt()
            val now = LocalDate.now().year
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
        birth_date = birthDate,
        gender = gender,
        phone = phone,
        prot_email = protPhone,
        prot_name = relation,
        email = email,
        isStaff = isStaff
    )
}