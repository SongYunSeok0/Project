// feature/auth/src/main/java/com/auth/viewmodel/AuthViewModel.kt
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

    /** 공용 메시지 */
    fun info(msg: String) { _events.tryEmit(msg) }
    /** 기존 UI 호환용 */
    fun emitInfo(msg: String) = info(msg)

    /** 로그인 */
    fun login(id: String, pw: String) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }
        val ok = runCatching { loginUseCase(id, pw) }.isSuccess
        _state.update { it.copy(loading = false, isLoggedIn = ok) }
        _events.tryEmit(if (ok) "로그인 성공" else "로그인 실패")
    }

    /** 회원가입 */
    fun signup(req: SignupRequest) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }
        val ok = runCatching { signupUseCase(req) }.getOrDefault(false)
        _state.update { it.copy(loading = false) }
        _events.tryEmit(if (ok) "회원가입 성공" else "회원가입 실패")
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
}
