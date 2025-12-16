package com.data.mapper

import com.data.network.dto.user.UserDto
import com.data.network.dto.user.UserUpdateDto
import com.domain.model.UserProfile
import java.time.LocalDate

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
        prot_email = prot_email,
        // [수정] 서버 DB의 'relation' 컬럼 값을 앱의 'prot_name'으로 매핑
        prot_name = relation,
        email = email
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
        prot_name = prot_name,
        relation = prot_name,
        email = email ?: ""
    )
}