package com.domain.usecase.auth

import com.domain.model.SocialLoginParam
import com.domain.repository.AuthRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        provider: String,
        socialId: String,
        accessToken: String?,
        idToken: String?
    ) = repo.socialLogin(
        SocialLoginParam(
            provider = provider,
            socialId = socialId,
            accessToken = accessToken,
            idToken = idToken
        )
    )   // Result<SocialLoginResult>
}
