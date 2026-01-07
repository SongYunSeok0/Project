package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.UserProfile
import com.domain.usecase.auth.ClearLocalAuthDataUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.WithdrawalUseCase
import com.domain.usecase.mypage.GetUserProfileUseCase
import com.domain.usecase.mypage.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val withdrawalUseCase: WithdrawalUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val clearLocalUserDataUseCase: ClearLocalAuthDataUseCase
) : ViewModel() {

    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile


    init {
        Log.e("MyPageViewModel", "🎬 ========== ViewModel 초기화 시작 ==========")
        loadProfile()

        viewModelScope.launch {
            Log.e("MyPageViewModel", "👂 observeLocalProfile 시작")
            observeUserProfileUseCase().collect { local ->
                Log.e("MyPageViewModel", "📥 로컬 Profile 수신: $local")
                if (local != null) {
                    _profile.value = local
                    Log.e("MyPageViewModel", "✅ Profile 업데이트 완료")
                } else {
                    Log.e("MyPageViewModel", "⚠️ 로컬 Profile이 null")
                }
            }
        }
    }

    fun loadProfile() = viewModelScope.launch {
        Log.e("MyPageViewModel", "📡 ========== loadProfile() 시작 ==========")
        runCatching { getUserProfileUseCase() }
            .onSuccess {
                Log.e("MyPageViewModel", "✅ Profile API 성공: $it")
                _profile.value = it
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ Profile API 실패: ${it.message}", it)
                _events.send(MyPageEvent.LoadFailed)
            }
    }


    fun refreshProfile() = viewModelScope.launch {
        Log.e("MyPageViewModel", "🔄 ========== refreshProfile() 시작 ==========")
        runCatching { getUserProfileUseCase() }
            .onSuccess {
                Log.e("MyPageViewModel", "✅ Profile 새로고침 성공: $it")
                _profile.value = it
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ Profile 새로고침 실패: ${it.message}", it)
            }
    }

    private var isLoggingOut = false

    fun logout() = viewModelScope.launch {
        if (isLoggingOut) {
            Log.e("MyPageViewModel", "⚠️ 이미 로그아웃 진행 중")
            return@launch
        }
        isLoggingOut = true

        Log.e("MyPageViewModel", "🚪 ========== 로그아웃 시작 ==========")
        runCatching { logoutUseCase() }
            .onSuccess {
                Log.e("MyPageViewModel", "✅ 로그아웃 성공")
                _events.send(MyPageEvent.LogoutSuccess)
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ 로그아웃 실패: ${it.message}", it)
                _events.send(MyPageEvent.LogoutFailed)
            }
            .also { isLoggingOut = false }
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