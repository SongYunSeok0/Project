package com.domain.model

sealed class SocialLoginResult {
    data class Success(val tokens: AuthTokens) : SocialLoginResult()
    object NeedAdditionalInfo : SocialLoginResult()
    data class Error(val message: String?) : SocialLoginResult()
}
