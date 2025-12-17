package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.usecase.mypage.RegisterDeviceResult
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

    fun updateSSID(v: String) {
        _state.value = _state.value.copy(ssid = v)
    }

    fun updatePW(v: String) {
        _state.value = _state.value.copy(pw = v)
    }

    fun updateDeviceName(v: String) {
        _state.value = _state.value.copy(deviceName = v)
    }

    fun setDeviceInfo(uuid: String, token: String) {
        _state.value = _state.value.copy(
            deviceUUID = uuid,
            deviceToken = token
        )
    }

    fun startRegister() = viewModelScope.launch {
        val s = state.value

        _state.value = _state.value.copy(
            loading = true,
            uiError = null
        )

        val result = registerDeviceUseCase(
            ssid = s.ssid,
            pw = s.pw,
            uuid = s.deviceUUID,
            token = s.deviceToken,
            deviceName = s.deviceName
        )

        _state.value = when (result) {
            is RegisterDeviceResult.Success -> {
                s.copy(
                    loading = false,
                    configSent = true
                )
            }

            is RegisterDeviceResult.Error -> {
                s.copy(
                    loading = false,
                    uiError = result.toUiError()
                )
            }
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



