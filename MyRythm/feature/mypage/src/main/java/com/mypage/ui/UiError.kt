package com.mypage.ui

import android.content.Context
import com.domain.usecase.mypage.RegisterDeviceResult
import com.shared.R

sealed interface UiError {
    data object NeedLogin : UiError
    data object BleFailed : UiError
    data object NetworkFailed : UiError
    data class Message(val text: String) : UiError
}

// MyPageScreen에서 사용하는 확장 함수
fun UiError.toMessage(context: Context): String {
    return when (this) {
        is UiError.NeedLogin -> context.getString(R.string.error_need_login)
        is UiError.BleFailed -> context.getString(R.string.error_ble_failed)
        is UiError.NetworkFailed -> context.getString(R.string.error_network_failed)
        is UiError.Message -> this.text
    }
}