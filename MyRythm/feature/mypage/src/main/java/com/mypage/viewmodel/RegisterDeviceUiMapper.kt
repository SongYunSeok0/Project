package com.mypage.viewmodel

import com.domain.usecase.mypage.RegisterDeviceResult
import com.shared.model.UiError

fun RegisterDeviceResult.Error.toUiError(): UiError =
    when (this) {
        is RegisterDeviceResult.Error.NotLoggedIn ->
            UiError.NeedLogin

        is RegisterDeviceResult.Error.BleConnectFailed,
        is RegisterDeviceResult.Error.BleSendFailed ->
            UiError.Message("BLE 연결에 실패했습니다")

        is RegisterDeviceResult.Error.Unknown ->
            UiError.Message(message)

        else ->
            UiError.Message("알 수 없는 오류")
    }
