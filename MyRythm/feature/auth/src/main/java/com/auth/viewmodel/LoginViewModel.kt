package com.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.exception.DomainException
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val uiState: StateFlow<UiState> = _uiState

    private val _autoLoginEnabled = MutableStateFlow(false)
    val autoLoginEnabled: StateFlow<Boolean> = _autoLoginEnabled

    fun setAutoLogin(enabled: Boolean) {
        _autoLoginEnabled.value = enabled
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "이메일과 비밀번호를 입력하세요") }
            return@launch
        }

        _uiState.update { it.copy(loading = true, errorMessage = null) }

        loginUseCase(email, password, _autoLoginEnabled.value)
            .onSuccess { tokens ->
                authPrefs.setAutoLoginEnabled(_autoLoginEnabled.value)
                val uid = JwtUtils.extractUserId(tokens.access) ?: ""

                _uiState.update {
                    it.copy(
                        loading = false,
                        isLoggedIn = true,
                        userId = uid
                    )
                }
            }
            .onFailure { error ->
                val message = when (error) {
                    is DomainException.AuthException -> "이메일 또는 비밀번호가 올바르지 않습니다"
                    is DomainException.NetworkException -> "인터넷 연결을 확인해주세요"
                    else -> "로그인에 실패했습니다"
                }
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
    }

    fun logout() = viewModelScope.launch {
        logoutUseCase()
            .onSuccess {
                _uiState.update { it.copy(isLoggedIn = false, userId = null) }
                _autoLoginEnabled.value = false
            }
            .onFailure { error ->
                _uiState.update { it.copy(errorMessage = "로그아웃 실패") }
            }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}