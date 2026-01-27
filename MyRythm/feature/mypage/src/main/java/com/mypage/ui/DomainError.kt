package com.mypage.ui

import com.domain.model.DomainError
import com.shared.model.UiError

fun DomainError.toUiError(): UiError =
    when (this) {
        is DomainError.Network ->
            UiError.NetworkFailed

        is DomainError.Server ->
            UiError.Message("서버 오류가 발생했어요")

        is DomainError.Auth ->
            UiError.Message(message)

        is DomainError.Conflict ->
            UiError.Message("이미 사용 중인 이메일이에요")

        is DomainError.NotFound ->
            UiError.Message("데이터를 찾을 수 없어요")

        is DomainError.Validation ->
            UiError.Message(message)

        is DomainError.InvalidToken ->
            UiError.NeedLogin

        is DomainError.NeedAdditionalInfo ->
            UiError.Message("추가 정보 입력이 필요해요")

        is DomainError.Unknown ->
            UiError.Message(message)
    }