package com.shared.model

sealed interface UiError {
    data object NeedLogin : UiError
    data object NetworkFailed : UiError
    data object ServerFailed : UiError
    data class Message(val text: String) : UiError
}