package com.data.mapper.user

import com.data.network.dto.user.UserDto
import com.domain.model.User

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
    preferences = preferences,
    protPhone = prot_phone,
    relation = relation,
    isActive = is_active,
    isStaff = is_staff,
    createdAt = created_at,
    updatedAt = updated_at,
    lastLogin = last_login
)
