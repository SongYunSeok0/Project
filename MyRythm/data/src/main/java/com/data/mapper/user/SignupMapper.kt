package com.data.mapper.user

import com.data.network.dto.user.UserSignupRequest
import com.domain.model.SignupRequest

fun SignupRequest.toDto(): UserSignupRequest =
    UserSignupRequest(
        email = email,
        username = username,
        phone = phone,
        birthDate = birthDate,
        gender = gender,
        height = height,
        weight = weight,
        password = password
    )
