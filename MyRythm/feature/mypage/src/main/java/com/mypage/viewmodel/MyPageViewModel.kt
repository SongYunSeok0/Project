package com.mypage.viewmodel

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.UserProfile
import com.domain.usecase.auth.ClearLocalAuthDataUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.WithdrawalUseCase
import com.domain.usecase.health.GetLatestHeartRateUseCase
import com.domain.usecase.inquiry.GetInquiriesUseCase
import com.domain.usecase.inquiry.AddInquiryUseCase
import com.domain.usecase.mypage.GetUserProfileUseCase
import com.domain.usecase.mypage.ObserveUserProfileUseCase
import com.mypage.ui.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val withdrawalUseCase: WithdrawalUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val clearLocalUserDataUseCase: ClearLocalAuthDataUseCase
) : ViewModel() {

    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState



    init {
        Log.e("MyPageViewModel", "🎬 ========== ViewModel 초기화 시작 ==========")
        loadProfile()

        viewModelScope.launch {
            Log.e("MyPageViewModel", "👂 observeLocalProfile 시작")
            observeUserProfileUseCase().collect { local ->
                Log.e("MyPageViewModel", "📥 로컬 Profile 수신: $local")
                _uiState.value = _uiState.value.copy(
                    profile = local
                )
            }
        }
    }

    fun loadProfile() = viewModelScope.launch {
        Log.e("MyPageViewModel", "📡 ========== loadProfile() 시작 ==========")
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )
        runCatching { getUserProfileUseCase() }
            .onSuccess { profile ->
                Log.e("MyPageViewModel", "✅ Profile API 성공: $profile")
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ Profile API 실패: ${it.message}", it)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = UiError.NetworkFailed
                )
                _events.send(MyPageEvent.LoadFailed)
            }
    }


    fun refreshProfile() = viewModelScope.launch {
        Log.e("MyPageViewModel", "🔄 ========== refreshProfile() 시작 ==========")
        runCatching { getUserProfileUseCase() }
            .onSuccess { profile ->
                Log.e("MyPageViewModel", "✅ Profile 새로고침 성공: $profile")
                _uiState.value = _uiState.value.copy(
                    profile = profile
                )
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ Profile 새로고침 실패: ${it.message}", it)
            }
    }

    fun deleteAccount() = viewModelScope.launch {
        Log.e("MyPageViewModel", "🗑️ ========== 회원 탈퇴 시작 ==========")

        when (val result = withdrawalUseCase()) {
            is ApiResult.Success -> {
                Log.e("MyPageViewModel", "✅ 회원 탈퇴 성공")

                // 🔥 로컬 사용자 데이터 정리
                clearLocalUserDataUseCase()

                _events.send(MyPageEvent.WithdrawalSuccess)
            }

            is ApiResult.Failure -> {
                Log.e("MyPageViewModel", "❌ 회원 탈퇴 실패: ${result.error}")
                _events.send(MyPageEvent.WithdrawalFailed)
            }
        }
    }
}