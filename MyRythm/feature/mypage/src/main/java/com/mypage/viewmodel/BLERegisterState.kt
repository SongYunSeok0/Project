package com.mypage.viewmodel

import com.mypage.ui.UiError

data class BLERegisterState(
    val loading: Boolean = false,
    val bleConnected: Boolean = false,
    val configSent: Boolean = false,
    val ssid: String = "",
    val pw: String = "",
    val deviceUUID: String = "",
    val deviceToken: String = "",
    val deviceName: String = "",
    val uiError: UiError? = null
)
