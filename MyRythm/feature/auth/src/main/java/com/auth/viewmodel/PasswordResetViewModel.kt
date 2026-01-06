package com.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.usecase.auth.ResetPasswordUseCase
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isCodeSent: Boolean = false,
        val isCodeVerified: Boolean = false,
        val isResetSuccess: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun sendResetCode(email: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        when (val result = sendEmailCodeUseCase(email, null)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(loading = false, isCodeSent = true) }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "인증코드 전송 실패")
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
        }
    }

    fun verifyResetCode(email: String, code: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        when (val result = verifyEmailCodeUseCase(email, code)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(loading = false, isCodeVerified = true) }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "인증 실패")
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        when (val result = resetPasswordUseCase(email, newPassword)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(loading = false, isResetSuccess = true) }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "비밀번호 재설정 실패")
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun mapErrorToMessage(error: DomainError, defaultMessage: String): String {
        return when (error) {
            is DomainError.NotFound -> "존재하지 않는 이메일입니다"
            is DomainError.Auth -> "인증코드가 올바르지 않습니다"
            is DomainError.Validation -> "비밀번호 형식이 올바르지 않습니다"
            is DomainError.Network -> "인터넷 연결을 확인해주세요"
            is DomainError.Server -> "서버 오류가 발생했습니다"
            is DomainError.Conflict -> "이미 사용 중인 비밀번호입니다"
            is DomainError.InvalidToken -> "토큰이 유효하지 않습니다"
            is DomainError.NeedAdditionalInfo -> "추가 정보가 필요합니다"
            is DomainError.Unknown -> defaultMessage
        }
    }
}