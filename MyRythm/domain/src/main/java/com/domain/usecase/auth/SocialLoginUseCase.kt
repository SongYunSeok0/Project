package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        param: SocialLoginParam,
        autoLogin: Boolean
    ): ApiResult<SocialLoginResult> {
        authRepository.saveAutoLoginEnabled(autoLogin)
        val result = authRepository.socialLogin(param)

        // 소셜 로그인 "성공"인 경우에만: 계정 전환 대비 로컬 캐시 정리 + 현재 계정으로 재동기화
        if (result is ApiResult.Success) {
            userRepository.clearProfile()
            userRepository.syncUser()
        }

        return result
    }
}
