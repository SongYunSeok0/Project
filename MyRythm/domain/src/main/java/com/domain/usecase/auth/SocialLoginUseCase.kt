package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        param: SocialLoginParam
    ): ApiResult<SocialLoginResult> =
        repository.socialLogin(param)
}


