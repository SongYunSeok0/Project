package com.mypage.ui

import com.domain.model.DomainError

fun DomainError.toUiError(): UiError =
    when (this) {

        is DomainError.Network ->
            UiError.NetworkFailed

        is DomainError.Server ->
            UiError.Message(message ?: "서버 오류가 발생했어요")

        is DomainError.Unknown ->
            UiError.Message(message ?: "알 수 없는 오류가 발생했어요")

        DomainError.EmailSendFailed ->
            UiError.Message("이메일 전송에 실패했어요")

        DomainError.VerifyCodeFailed ->
            UiError.Message("인증에 실패했어요")

        DomainError.DuplicateEmail ->
            UiError.Message("이미 사용 중인 이메일이에요")

        DomainError.NeedAdditionalInfo ->
            UiError.Message("추가 정보 입력이 필요해요")

        is DomainError.InvalidToken ->
            UiError.NeedLogin
    }
