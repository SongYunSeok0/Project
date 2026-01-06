package com.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.model.DomainError
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
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
    private val tokenStore: TokenStore,
    private val authPrefs: AuthPreferencesDataSource
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _autoLoginEnabled = MutableStateFlow(false)
    val autoLoginEnabled: StateFlow<Boolean> = _autoLoginEnabled.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() = viewModelScope.launch {
        // 저장된 자동 로그인 설정 불러오기
        _autoLoginEnabled.value = authPrefs.isAutoLoginEnabled()

        // 저장된 토큰 확인
        val currentTokens = tokenStore.current()
        val token = currentTokens.access

        if (!token.isNullOrBlank()) {
            val userId = JwtUtils.extractUserId(token)
            if (userId != null) {
                _uiState.update {
                    it.copy(isLoggedIn = true, userId = userId)
                }
            } else {
                // 토큰이 있지만 유효하지 않은 경우 제거
                tokenStore.clear()
            }
        }
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

        when (val result = loginUseCase(email, password, _autoLoginEnabled.value)) {
            is com.domain.model.ApiResult.Success -> {
                authPrefs.setAutoLoginEnabled(_autoLoginEnabled.value)
                val uid = JwtUtils.extractUserId(result.data.access) ?: ""

                _uiState.update {
                    it.copy(
                        loading = false,
                        isLoggedIn = true,
                        userId = uid,
                        errorMessage = null
                    )
                }
            }
            is com.domain.model.ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error)
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        when (val result = logoutUseCase()) {
            is com.domain.model.ApiResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        userId = null,
                        errorMessage = null
                    )
                }
                // 로그아웃 시 자동 로그인 설정도 해제
                _autoLoginEnabled.value = false
                authPrefs.setAutoLoginEnabled(false)
            }
            is com.domain.model.ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error)
                _uiState.update { it.copy(errorMessage = message) }
            }
        }
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