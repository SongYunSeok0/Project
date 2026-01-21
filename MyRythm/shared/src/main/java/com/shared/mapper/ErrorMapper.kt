package com.shared.mapper

import com.domain.model.DomainError
import com.shared.model.UiError

fun DomainError.toUiError(): UiError =
    when (this) {
        is DomainError.Network ->
            UiError.NetworkFailed

        is DomainError.Server ->
            UiError.ServerFailed

        is DomainError.Unknown ->
            UiError.Message(message)

        is DomainError.Auth ->
            UiError.NeedLogin

        is DomainError.InvalidToken ->
            UiError.NeedLogin

        is DomainError.Validation ->
            UiError.Message(message)

        is DomainError.Conflict ->
            UiError.Message("이미 존재하는 데이터입니다")

        is DomainError.NotFound ->
            UiError.Message("데이터를 찾을 수 없습니다")

        is DomainError.NeedAdditionalInfo ->
            UiError.Message("추가 정보 입력이 필요해요")
    }