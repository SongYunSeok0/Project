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
            UiError.Message(message)

        // -------- Auth --------
        is DomainError.Auth ->
            UiError.NeedLogin

        is DomainError.InvalidToken ->
            UiError.NeedLogin

        // -------- Validation --------
        is DomainError.Validation ->
            UiError.Message(message)

        // -------- Conflict --------
        is DomainError.Conflict ->
            UiError.Message("이미 존재하는 데이터입니다")

        // -------- NotFound --------
        is DomainError.NotFound ->
            UiError.Message("데이터를 찾을 수 없습니다")

        // -------- Social --------
        DomainError.NeedAdditionalInfo ->
            UiError.Message("추가 정보 입력이 필요해요")
    }