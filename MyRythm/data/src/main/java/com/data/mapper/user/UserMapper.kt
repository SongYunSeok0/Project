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
        prot_name = relation,  // ✅ relation 필드를 매핑
        prot_email = prot_email,
        email = email,
        isStaff = is_staff
    )
}

fun UserProfile.toDto(): UserUpdateDto {
    return UserUpdateDto(
        username = username ?: "",
        height = height,
        weight = weight,
        gender = gender,
        birth_date = birth_date,
        phone = phone,
        prot_email = prot_email,
        prot_name = prot_name,  // prot_name으로 전송
        relation = prot_name,   // ✅ relation 필드도 함께 전송 (서버 호환성)
        email = email ?: ""
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
        prot_name = relation,  // ✅ relation 필드를 매핑
        email = email,
        isStaff = isStaff
    )
}