package com.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.exception.DomainException
import com.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isCodeSent: Boolean = false,
        val isCodeVerified: Boolean = false,
        val isResetSuccess: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun sendResetCode(email: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        authRepository.sendEmailCode(email, null)
            .onSuccess {
                _uiState.update { it.copy(loading = false, isCodeSent = true) }
            }
            .onFailure { error ->
                val message = when (error) {
                    is DomainException.NotFoundException -> "존재하지 않는 이메일입니다"
                    is DomainException.NetworkException -> "인터넷 연결을 확인해주세요"
                    else -> "인증코드 전송 실패"
                }
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
    }

    fun verifyResetCode(email: String, code: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        authRepository.verifyEmailCode(email, code)
            .onSuccess {
                _uiState.update { it.copy(loading = false, isCodeVerified = true) }
            }
            .onFailure { error ->
                val message = when (error) {
                    is DomainException.AuthException -> "인증코드가 올바르지 않습니다"
                    else -> "인증 실패"
                }
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
    }

    fun resetPassword(email: String, newPassword: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        authRepository.resetPassword(email, newPassword)
            .onSuccess {
                _uiState.update { it.copy(loading = false, isResetSuccess = true) }
            }
            .onFailure { error ->
                val message = when (error) {
                    is DomainException.ValidationException -> "비밀번호 형식이 올바르지 않습니다"
                    is DomainException.NetworkException -> "인터넷 연결을 확인해주세요"
                    else -> "비밀번호 재설정 실패"
                }
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}