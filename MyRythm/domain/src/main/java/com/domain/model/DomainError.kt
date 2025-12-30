package com.domain.model

sealed class DomainError {
    // 공통
    data class Network(val message: String? = null) : DomainError()
    data class Server(val code: Int, val message: String? = null) : DomainError()
    data class Unknown(val message: String? = null) : DomainError()

    // Auth 도메인
    object EmailSendFailed : DomainError()
    object VerifyCodeFailed : DomainError()
    object DuplicateEmail : DomainError()

    // Social
    object NeedAdditionalInfo : DomainError()
    data class InvalidToken(val reason: String) : DomainError()
}
