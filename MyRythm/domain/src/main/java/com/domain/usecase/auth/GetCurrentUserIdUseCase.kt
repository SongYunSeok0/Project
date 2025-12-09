package com.domain.usecase.auth

import javax.inject.Inject

interface AuthTokenProvider {
    fun getCurrentUserId(): Long?
}

class GetCurrentUserIdUseCase @Inject constructor(
    private val authTokenProvider: AuthTokenProvider
) {
    operator fun invoke(): Long? {
        return authTokenProvider.getCurrentUserId()
    }
}