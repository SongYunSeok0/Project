package com.domain.usecase.auth

import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        provider: String,
        socialId: String,
        accessToken: String? = null,
        idToken: String? = null
    ): SocialLoginResult {
        return repository.socialLogin(SocialLoginParam(provider, socialId, accessToken, idToken))
    }
}