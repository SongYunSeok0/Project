package com.data.mapper.auth

import com.data.network.dto.user.LoginResponse
import com.domain.model.AuthTokens

fun LoginResponse.asAuthTokens(): AuthTokens =
    AuthTokens(
        access = access ?: "",
        refresh = refresh ?: ""
    )
