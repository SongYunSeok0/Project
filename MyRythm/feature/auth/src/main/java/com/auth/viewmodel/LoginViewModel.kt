package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.DomainError
import com.domain.usecase.auth.GetAuthStatusUseCase
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.model.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null,
        val errorMessage: String? = null,
        val isInitializing: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _autoLoginEnabled = MutableStateFlow(false)
    val autoLoginEnabled: StateFlow<Boolean> = _autoLoginEnabled.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() = viewModelScope.launch {
        Log.e("LoginViewModel", "🔍 ========== checkLoginStatus() 시작 ==========")

        when (val result = getAuthStatusUseCase()) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(
                        loading = false,
                        isLoggedIn = result.data.isLoggedIn,
                        userId = result.data.userId,
                        isInitializing = false
                    )
                }
                Log.e(
                    "LoginViewModel",
                    "✅ 로그인 상태 로드 완료: isLoggedIn=${result.data.isLoggedIn}, userId=${result.data.userId}"
                )
            }

            is ApiResult.Failure -> {
                _uiState.update {
                    it.copy(
                        loading = false,
                        isLoggedIn = false,
                        userId = null,
                        isInitializing = false
                    )
                }
                Log.e("LoginViewModel", "❌ 로그인 상태 로드 실패: ${result.error}")
            }
        }

        Log.e(
            "LoginViewModel",
            "🔍 checkLoginStatus() 완료, 최종 isLoggedIn=${_uiState.value.isLoggedIn}"
        )
    }


    fun setAutoLogin(enabled: Boolean) {
        _autoLoginEnabled.value = enabled
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        // 입력값 검증
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "이메일과 비밀번호를 입력하세요") }
            return@launch
        }

        _uiState.update { it.copy(loading = true, errorMessage = null) }

        Log.e("LoginViewModel", "🔐 로그인 시도 시작")

        when (val result = loginUseCase(email, password, _autoLoginEnabled.value)) {
            is ApiResult.Success -> {
                Log.e("LoginViewModel", "✅ LoginUseCase 성공")
                checkLoginStatus()

                Log.e("LoginViewModel", "✅ uiState 업데이트 완료: ${_uiState.value}")
            }

            is ApiResult.Failure -> {
                Log.e("LoginViewModel", "❌ LoginUseCase 실패: ${result.error}")
                val message = mapErrorToMessage(result.error)
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        Log.e("LoginViewModel", "🚪 logout() 시작")

        when (val result = logoutUseCase()) {
            is ApiResult.Success -> {
                Log.e("LoginViewModel", "✅ LogoutUseCase 성공")
            }
            is ApiResult.Failure -> {
                Log.e("LoginViewModel", "⚠️ LogoutUseCase 실패: ${result.error}")
            }
        }

        checkLoginStatus()

        _autoLoginEnabled.value = false
    }


    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun mapErrorToMessage(error: DomainError): String {
        return when (error) {
            is DomainError.Auth -> "이메일 또는 비밀번호가 올바르지 않습니다"
            is DomainError.Network -> "인터넷 연결을 확인해주세요"
            is DomainError.Validation -> error.message
            is DomainError.Server -> "서버 오류가 발생했습니다"
            is DomainError.Conflict -> "이미 존재하는 계정입니다"
            is DomainError.NotFound -> "사용자를 찾을 수 없습니다"
            is DomainError.InvalidToken -> "로그인 세션이 만료되었습니다"
            is DomainError.NeedAdditionalInfo -> "추가 정보가 필요합니다"
            is DomainError.Unknown -> "로그인에 실패했습니다"
        }
    }
}