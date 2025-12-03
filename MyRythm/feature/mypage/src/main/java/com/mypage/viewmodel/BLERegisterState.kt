package com.mypage.viewmodel

data class BLERegisterState(
    val loading: Boolean = false,
    val bleConnected: Boolean = false,
    val configSent: Boolean = false,
    val ssid: String = "",
    val pw: String = "",
    val error: String? = null
)
