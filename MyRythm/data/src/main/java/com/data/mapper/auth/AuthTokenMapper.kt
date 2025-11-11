package com.data.mapper.auth

import com.domain.model.AuthTokens as DomainTokens
import com.core.auth.AuthTokens as CoreTokens

fun DomainTokens.toCore(): CoreTokens = CoreTokens(
    access = access,
    refresh = refresh
)

fun CoreTokens.toDomain(): DomainTokens = DomainTokens(
    access = access,
    refresh = refresh
)
