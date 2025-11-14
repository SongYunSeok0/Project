package com.data.mapper.auth

import com.domain.model.SocialLoginParam
import com.domain.model.AuthTokens
import com.data.network.dto.user.SocialLoginRequest as SocialLoginRequestDto
import com.data.network.dto.user.SocialLoginResponse

// Domain -> DTO
fun SocialLoginParam.toDto(): SocialLoginRequestDto =
    SocialLoginRequestDto(
        provider = provider,
        socialId = socialId,
        accessToken = accessToken,
        idToken = idToken,
        email = email,
        name = name,
        profileImageUrl = profileImageUrl
    )

fun SocialLoginResponse.toDomainTokens(): AuthTokens =
    AuthTokens(
        access = this.access,
        refresh = this.refresh
    )