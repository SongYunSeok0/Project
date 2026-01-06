package com.scheduler.ui

import com.domain.model.DomainError

sealed interface UiError {
    data object NeedLogin : UiError
    data object NetworkFailed : UiError
    data object ServerFailed : UiError
    data class Message(val text: String) : UiError
}

fun DomainError.toUiError(): UiError =
    when (this) {

        // -------- 공통 --------
        is DomainError.Network ->
            UiError.NetworkFailed

        is DomainError.Server ->
            UiError.ServerFailed

        is DomainError.Unknown ->
            UiError.Message(message ?: "알 수 없는 오류가 발생했어요")

        // -------- Auth --------
        DomainError.EmailSendFailed ->
            UiError.Message("이메일 전송에 실패했어요")

        DomainError.VerifyCodeFailed ->
            UiError.Message("인증 코드가 올바르지 않아요")

        DomainError.DuplicateEmail ->
            UiError.Message("이미 사용 중인 이메일이에요")

        // -------- Social --------
        DomainError.NeedAdditionalInfo ->
            UiError.Message("추가 정보 입력이 필요해요")

        is DomainError.InvalidToken ->
            UiError.NeedLogin
    }
