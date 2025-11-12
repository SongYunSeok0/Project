package com.domain.usecase.auth

import com.domain.model.SocialLoginRequest
import com.data.network.dto.user.LoginResponse

import com.domain.repository.UserRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        provider: String,
        socialId: String,
        accessToken: String? = null,
        idToken: String? = null
    ): LoginResponse? {
        val request = SocialLoginRequest(provider, socialId, accessToken, idToken)
        return repository.socialLogin(request)
    }
}
