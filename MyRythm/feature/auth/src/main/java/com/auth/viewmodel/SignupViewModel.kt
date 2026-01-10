package com.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.model.SignupRequest
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
import com.domain.usecase.user.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val signupUseCase: SignupUseCase
) : ViewModel() {

    data class SignupForm(
        val email: String = "",
        val code: String = "",
        val username: String = "",
        val phone: String = "",
        val birthDate: String = "",
        val gender: String = "",
        val height: Double = 0.0,
        val weight: Double = 0.0,
        val password: String = "",
        val socialId: String? = null,
        val provider: String? = null
    )

    data class UiState(
        val loading: Boolean = false,
        val isCodeSent: Boolean = false,
        val isCodeVerified: Boolean = false,
        val isSignupSuccess: Boolean = false,
        val errorMessage: String? = null
    )

    private val _signupForm = MutableStateFlow(SignupForm())
    val signupForm: StateFlow<SignupForm> = _signupForm.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun updateEmail(v: String) = _signupForm.update { it.copy(email = v) }
    fun updateCode(v: String) = _signupForm.update { it.copy(code = v) }
    fun updateUsername(v: String) = _signupForm.update { it.copy(username = v) }
    fun updatePhone(v: String) = _signupForm.update { it.copy(phone = v) }
    fun updateBirth(v: String) = _signupForm.update { it.copy(birthDate = v) }
    fun updateGender(v: String) = _signupForm.update { it.copy(gender = v) }
    fun updateHeight(v: Double) = _signupForm.update { it.copy(height = v) }
    fun updateWeight(v: Double) = _signupForm.update { it.copy(weight = v) }
    fun updatePassword(v: String) = _signupForm.update { it.copy(password = v) }

    fun setSocialLoginInfo(socialId: String, provider: String) {
        _signupForm.update {
            it.copy(socialId = socialId, provider = provider)
        }
    }

    fun sendCode() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        when (val result = sendEmailCodeUseCase(_signupForm.value.email, _signupForm.value.username)) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(loading = false, isCodeSent = true)
                }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "인증코드 전송 실패")
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun verifyCode() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        val f = _signupForm.value
        when (val result = verifyEmailCodeUseCase(f.email, f.code)) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(loading = false, isCodeVerified = true)
                }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "인증 실패")
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun signup() = viewModelScope.launch {
        val f = _signupForm.value
        val request = SignupRequest(
            email = f.email,
            username = f.username,
            phone = f.phone,
            birthDate = f.birthDate,
            gender = f.gender,
            height = f.height,
            weight = f.weight,
            password = f.password
        )

        _uiState.update { it.copy(loading = true, errorMessage = null) }

        when (val result = signupUseCase(request)) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(loading = false, isSignupSuccess = true)
                }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "회원가입에 실패했습니다")
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun signup(request: SignupRequest) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, errorMessage = null) }

        when (val result = signupUseCase(request)) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(loading = false, isSignupSuccess = true)
                }
            }
            is ApiResult.Failure -> {
                val message = mapErrorToMessage(result.error, "회원가입에 실패했습니다")
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun mapErrorToMessage(error: DomainError, defaultMessage: String): String {
        return when (error) {
            is DomainError.Conflict -> "이미 존재하는 이메일입니다"
            is DomainError.Auth -> "인증코드가 올바르지 않습니다"
            is DomainError.Validation -> error.message
            is DomainError.Network -> "인터넷 연결을 확인해주세요"
            is DomainError.NotFound -> "존재하지 않는 사용자입니다"
            is DomainError.Server -> "서버 오류가 발생했습니다"
            is DomainError.InvalidToken -> "토큰이 유효하지 않습니다"
            is DomainError.NeedAdditionalInfo -> "추가 정보가 필요합니다"
            is DomainError.Unknown -> defaultMessage
        }
    }
}