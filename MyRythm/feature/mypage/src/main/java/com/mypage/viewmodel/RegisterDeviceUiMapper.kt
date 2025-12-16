package com.mypage.viewmodel

import com.domain.usecase.mypage.RegisterDeviceResult
import com.mypage.ui.UiError

fun RegisterDeviceResult.Error.toUiError(): UiError =
    when (this) {
        is RegisterDeviceResult.Error.NotLoggedIn ->
            UiError.NeedLogin

        is RegisterDeviceResult.Error.BleConnectFailed,
        is RegisterDeviceResult.Error.BleSendFailed ->
            UiError.BleFailed

        is RegisterDeviceResult.Error.Unknown ->
            UiError.Message(message)

        else ->
            UiError.Message("알 수 없는 오류")
    }
