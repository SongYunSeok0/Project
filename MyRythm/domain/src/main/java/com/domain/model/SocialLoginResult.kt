package com.domain.model

sealed class SocialLoginResult {

    data class Success(
        val tokens: AuthTokens
    ) : SocialLoginResult()

    /** 🔥 socialId + provider 를 담을 수 있는 구조로 변경 */
    data class NeedAdditionalInfo(
        val socialId: String,
        val provider: String
    ) : SocialLoginResult()

    data class Error(
        val message: String?
    ) : SocialLoginResult()
}

