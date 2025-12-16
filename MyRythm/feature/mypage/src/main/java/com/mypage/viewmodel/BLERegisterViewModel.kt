package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.device.BLEManager
import com.domain.repository.DeviceRepository
import com.domain.repository.UserRepository
import com.domain.usecase.mypage.RegisterDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BLERegisterViewModel @Inject constructor(
    private val registerDeviceUseCase: RegisterDeviceUseCase
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
        val s = state.value

        if (s.deviceName.isBlank()) {
            _state.value = _state.value.copy(error = "디바이스 별명을 입력해줘!")
            return@launch
        }

        _state.value = _state.value.copy(
            loading = true,
            error = null
        )

        try {
            registerDeviceUseCase.execute(
                ssid = s.ssid,
                pw = s.pw,
                uuid = s.deviceUUID,
                token = s.deviceToken,
                deviceName = s.deviceName
            )

            _state.value = _state.value.copy(
                loading = false,
                configSent = true
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = e.message ?: "디바이스 등록 실패"
            )
        }
    }


    fun resetFields() {
        _state.value = _state.value.copy(
            ssid = "",
            pw = "",
            deviceName = ""
        )
    }

    fun resetAll() {
        _state.value = BLERegisterState()
    }

}


