package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.device.BLEManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BLERegisterViewModel @Inject constructor(
    private val ble: BLEManager
) : ViewModel() {

    private val _state = MutableStateFlow(BLERegisterState())
    val state: StateFlow<BLERegisterState> = _state

    fun updateSSID(v: String) {
        _state.value = _state.value.copy(ssid = v)
    }

    fun updatePW(v: String) {
        _state.value = _state.value.copy(pw = v)
    }

    fun startRegister() = viewModelScope.launch {
        val ssid = state.value.ssid
        val pw = state.value.pw
        val uuid = state.value.deviceUUID
        val token = state.value.deviceToken

        if (ssid.isBlank() || pw.isBlank()) {
            _state.value = _state.value.copy(
                error = "SSID 또는 비밀번호가 비어있어!"
            )
            return@launch
        }

        _state.value = _state.value.copy(
            loading = true,
            error = null,
            bleConnected = false,
            configSent = false
        )

        // 1) BLE 연결
        val connected = ble.scanAndConnectSuspend()
        if (!connected) {
            ble.disconnect()
            _state.value = _state.value.copy(
                loading = false,
                error = "BLE 연결 실패"
            )
            return@launch
        }

        _state.value = _state.value.copy(bleConnected = true)

        // 2) JSON (줄바꿈 없이)
        val json = """{"uuid":"$uuid","token":"$token","ssid":"$ssid","pw":"$pw"}"""

        val sent = ble.sendConfigSuspend(json)

        if (!sent) {
            ble.disconnect()
            _state.value = _state.value.copy(
                loading = false,
                error = "Wi-Fi 정보 전송 실패"
            )
            return@launch
        }

        _state.value = _state.value.copy(
            loading = false,
            configSent = true
        )
    }



    fun setDeviceInfo(uuid: String, token: String) {
        _state.value = _state.value.copy(
            deviceUUID = uuid,
            deviceToken = token
        )
    }

}
