package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.device.BLEManager
import com.domain.repository.DeviceRepository
import com.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BLERegisterViewModel @Inject constructor(
    private val ble: BLEManager,
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BLERegisterState())
    val state: StateFlow<BLERegisterState> = _state

    fun updateSSID(v: String) { _state.value = _state.value.copy(ssid = v) }
    fun updatePW(v: String) { _state.value = _state.value.copy(pw = v) }
    fun updateDeviceName(v: String) { _state.value = _state.value.copy(deviceName = v) }

    fun setDeviceInfo(uuid: String, token: String) {
        _state.value = _state.value.copy(
            deviceUUID = uuid,
            deviceToken = token
        )
    }

    fun startRegister() = viewModelScope.launch {

        val userId = userRepository.getLocalUser()?.id
        if (userId == null) {
            _state.value = _state.value.copy(
                loading = false,
                error = "로그인이 필요해!"
            )
            return@launch
        }

        val s = state.value
        val ssid = s.ssid
        val pw = s.pw
        val uuid = s.deviceUUID
        val token = s.deviceToken
        val deviceName = s.deviceName

        if (deviceName.isBlank()) {
            _state.value = _state.value.copy(error = "디바이스 별명을 입력해줘!")
            return@launch
        }

        _state.value = _state.value.copy(
            loading = true,
            error = null
        )

        try {
            deviceRepository.registerDevice(
                uuid = uuid,
                token = token,
                name = deviceName
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = "디바이스 별명 저장 실패: ${e.message}"
            )
            return@launch
        }

        // --- 이후 BLE 시작 ---
        val connected = ble.scanAndConnectSuspend()
        if (!connected) {
            ble.disconnect()
            _state.value = _state.value.copy(
                loading = false,
                error = "BLE 연결 실패"
            )
            return@launch
        }

        val json = """{"uuid":"$uuid","token":"$token","ssid":"$ssid","pw":"$pw"}"""
        Log.d("BLE_REGISTER", "Send JSON → $json")

        val sent = ble.sendConfigSuspend(json)

        if (!sent) {
            ble.disconnect()
            _state.value = _state.value.copy(
                loading = false,
                error = "기기 전송 실패"
            )
            return@launch
        }

        _state.value = _state.value.copy(
            loading = false,
            configSent = true
        )
    }

    fun resetFields() {
        _state.value = _state.value.copy(
            ssid = "",
            pw = "",
            deviceName = ""
        )
    }

}


