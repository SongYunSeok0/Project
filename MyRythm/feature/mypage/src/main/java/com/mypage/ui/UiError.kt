package com.mypage.ui

sealed interface UiError {
    data object NeedLogin : UiError
    data object BleFailed : UiError
    data object NetworkFailed : UiError
    data class Message(val text: String) : UiError
}
