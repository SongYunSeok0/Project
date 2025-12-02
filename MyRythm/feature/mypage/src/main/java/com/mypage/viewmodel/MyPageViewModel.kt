package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.UserProfile
import com.domain.repository.InquiryRepository
import com.domain.repository.AuthRepository
import com.domain.repository.DeviceRepository
import com.domain.repository.ProfileRepository
import com.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val inquiryRepository: InquiryRepository,
    private val userRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    val inquiries = inquiryRepository.getInquiries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadProfile()

        viewModelScope.launch {
            userRepository.observeLocalProfile().collect { local ->
                if (local != null) {
                    _profile.value = local
                }
            }
        }
    }

    fun loadProfile() = viewModelScope.launch {
        runCatching { userRepository.getProfile() }
            .onSuccess { _profile.value = it }
            .onFailure { _events.send(MyPageEvent.LoadFailed) }
    }

    fun refreshProfile() = viewModelScope.launch {
        runCatching { userRepository.getProfile() }
            .onSuccess { _profile.value = it }
    }

    private var isLoggingOut = false

    fun logout() = viewModelScope.launch {
        if (isLoggingOut) return@launch
        isLoggingOut = true

        Log.d("MyPageVM", "로그아웃 시작")
        runCatching { logoutUseCase() }
            .onSuccess {
                Log.d("MyPageVM", "로그아웃 성공")
                _events.send(MyPageEvent.LogoutSuccess)
            }
            .onFailure {
                Log.e("MyPageVM", "로그아웃 실패", it)
                _events.send(MyPageEvent.LogoutFailed)
            }
            .also { isLoggingOut = false }
    }

    fun addInquiry(type: String, title: String, content: String) {
        viewModelScope.launch {
            runCatching { inquiryRepository.addInquiry(type, title, content) }
                .onSuccess { _events.send(MyPageEvent.InquirySubmitSuccess) }
                .onFailure { e ->
                    _events.send(MyPageEvent.InquirySubmitFailed(e.message ?: "문의 실패"))
                }
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        runCatching { authRepository.withdrawal() }
            .onSuccess {
                if (it) _events.send(MyPageEvent.WithdrawalSuccess)
                else _events.send(MyPageEvent.WithdrawalFailed)
            }
            .onFailure { _events.send(MyPageEvent.WithdrawalFailed) }
    }

    fun requestDeviceRegister() {
        viewModelScope.launch {
            try {
                val device = deviceRepository.registerDevice()
                _events.send(MyPageEvent.DeviceRegisterSuccess(device.uuid))
            } catch (e: Exception) {
                _events.send(MyPageEvent.DeviceRegisterFailed)
            }
        }
    }

    init {
        loadProfile()
        refreshHeartData()
    }

}
