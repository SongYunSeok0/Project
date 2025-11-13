package com.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.SignupRequest
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.RefreshTokenUseCase
import com.domain.usecase.user.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signupUseCase: SignupUseCase,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false
    )

    private val _state = MutableStateFlow(UiState(isLoggedIn = false))
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    fun info(msg: String) { _events.tryEmit(msg) }
    fun emitInfo(msg: String) = info(msg)

    /** 로그인: email/password로 고정 */
    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }
        val result = runCatching { loginUseCase(email, password) }
        val ok = result.isSuccess
        _state.update { it.copy(loading = false, isLoggedIn = ok) }
        if (ok) {
            _events.tryEmit("로그인 성공")
        } else {
            _events.tryEmit(parseError(result.exceptionOrNull()) ?: "로그인 실패")
        }
    }

    /** 회원가입 */
    fun signup(req: SignupRequest) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }
        val result = runCatching { signupUseCase(req) }
        _state.update { it.copy(loading = false) }
        val ok = result.getOrDefault(false)
        if (ok) {
            _events.tryEmit("회원가입 성공")
        } else {
            _events.tryEmit(parseError(result.exceptionOrNull()) ?: "회원가입 실패")
        }
    }

    /** 토큰 갱신 */
    fun tryRefresh() = viewModelScope.launch(Dispatchers.IO) {
        val ok = runCatching { refreshUseCase() }.getOrDefault(false)
        if (ok) _events.tryEmit("토큰 갱신")
    }

    /** 로그아웃 */
    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { logoutUseCase() }
        _state.update { it.copy(isLoggedIn = false) }
        _events.tryEmit("로그아웃 완료")
    }

    /** HttpException 본문 메시지 우선 노출 */
    private fun parseError(t: Throwable?): String? {
        if (t == null) return null
        return when (t) {
            is HttpException -> {
                val code = t.code()
                val body = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                body?.takeIf { it.isNotBlank() } ?: "HTTP $code"
            }
            else -> t.message
        }
    }
}
