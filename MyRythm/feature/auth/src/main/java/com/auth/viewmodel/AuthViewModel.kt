package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.data.core.push.PushManager
import com.domain.model.SignupRequest
import com.domain.repository.AuthRepository
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.RefreshTokenUseCase
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.domain.usecase.user.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signupUseCase: SignupUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val tokenStore: TokenStore,
    private val repo: AuthRepository
) : ViewModel() {

    // -----------------------------------------------------------
    // SignupForm (회원가입 입력 폼)
    // -----------------------------------------------------------
    data class SignupForm(
        val email: String = "",
        val code: String = "",
        val username: String = "",
        val phone: String = "",
        val birthDate: String = "",
        val gender: String = "",
        val height: Double = 0.0,
        val weight: Double = 0.0,
        val password: String = ""
    )

    private val _signupForm = MutableStateFlow(SignupForm())
    val signupForm: StateFlow<SignupForm> = _signupForm
    
    fun updateSignupEmail(v: String) =
        _signupForm.update { it.copy(email = v) }

    fun updateCode(v: String) =
        _signupForm.update { it.copy(code = v) }

    fun updateSignupPassword(v: String) =
        _signupForm.update { it.copy(password = v) }


    // -----------------------------------------------------------
    // 로그인 / 상태 관리
    // -----------------------------------------------------------
    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null
    )

    data class FormState(
        val email: String = "",
        val password: String = ""
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    private fun emit(msg: String) = _events.tryEmit(msg)

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form

    fun updateLoginEmail(v: String) = _form.update { it.copy(email = v) }
    fun updateLoginPW(v: String) = _form.update { it.copy(password = v) }


    // -----------------------------------------------------------
    // 이메일 인증
    // -----------------------------------------------------------
    fun sendCode() = viewModelScope.launch {
        val ok = repo.sendEmailCode(signupForm.value.email)
        emit(if (ok) "인증코드 전송" else "전송 실패")
    }

    fun verifyCode() = viewModelScope.launch {
        val f = signupForm.value
        val ok = repo.verifyEmailCode(f.email, f.code)
        emit(if (ok) "인증 성공" else "인증 실패")
    }


    // -----------------------------------------------------------
    // 회원가입 처리
    // -----------------------------------------------------------
    fun signup(req: SignupRequest) = viewModelScope.launch {
        _state.update { it.copy(loading = true) }

        val ok = runCatching { signupUseCase(req) }.getOrDefault(false)

        _state.update { it.copy(loading = false) }
        emit(if (ok) "회원가입 성공" else "회원가입 실패")
    }


    // -----------------------------------------------------------
    // 로그인
    // -----------------------------------------------------------
    fun login() = viewModelScope.launch {
        val email = form.value.email
        val pw = form.value.password

        if (email.isBlank() || pw.isBlank()) {
            emit("ID와 비번을 입력하세요")
            return@launch
        }

        _state.update { it.copy(loading = true) }

        val result = loginUseCase(email, pw)
        val tokens = result.getOrNull()

        if (tokens != null) {

            val access = tokens.access ?: ""
            Log.e("AuthViewModel", "🔥 Access Token = $access")

            // 안전하게 분리
            val parts = access.split(".")
            if (parts.size >= 2) {
                try {
                    val payload = String(
                        android.util.Base64.decode(
                            parts[1],
                            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                        )
                    )
                    Log.e("AuthViewModel", "🔥 JWT Payload = $payload")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "❌ JWT decode 실패: ${e.message}")
                }
            } else {
                Log.e("AuthViewModel", "❌ JWT 형식 오류: $access")
            }

            val uid = JwtUtils.extractUserId(access)
            Log.e("AuthViewModel", "🔥 extractUserId() 결과 = $uid")

            PushManager.fcmToken?.let { token ->
                runCatching { registerFcmTokenUseCase(token) }
            }

            _state.update {
                it.copy(
                    loading = false,
                    isLoggedIn = true,
                    userId = uid
                )
            }

            emit("로그인 성공")
        } else {
            _state.update { it.copy(loading = false, isLoggedIn = false) }
            emit("이메일 또는 비밀번호가 올바르지 않습니다.")
        }
    }


    fun logout() = viewModelScope.launch {
        runCatching { logoutUseCase() }
        _state.update { it.copy(isLoggedIn = false) }
        emit("로그아웃 완료")
    }

    // -----------------------------------------------------------
    // 소셜 로그인(생략: 기존 코드 그대로 유지)
    // -----------------------------------------------------------

    private fun parseError(t: Throwable?): String {
        if (t == null) return "알 수 없는 오류"
        return when (t) {
            is HttpException -> "HTTP ${t.code()}"
            else -> t.message ?: "알 수 없는 오류"
        }
    }
}
