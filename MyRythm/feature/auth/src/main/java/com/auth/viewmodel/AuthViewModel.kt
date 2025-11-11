package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.auth.TokenStore
import com.data.network.api.UserApi
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.UserSignupRequest
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
    private val api: UserApi,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val isOfflineMode = false

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false
    )

    private val _state = MutableStateFlow(
        UiState(isLoggedIn = tokenStore.current().access != null)
    )
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    fun emitInfo(msg: String) {
        _events.tryEmit(msg)
    }

    fun login(id: String, pw: String) {
        if (isOfflineMode) {
            viewModelScope.launch(Dispatchers.IO) {
                tokenStore.set("offline-access", "offline-refresh")
                _state.update { it.copy(loading = false, isLoggedIn = true) }
                _events.tryEmit("오프라인 로그인 성공")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(loading = true) }
                val res = api.login(UserLoginRequest(id, pw))
                val body = res.body()
                val ok = res.isSuccessful && body?.access != null
                if (ok && body != null) tokenStore.set(body.access, body.refresh)

                _state.update { it.copy(loading = false, isLoggedIn = ok) }
                _events.tryEmit(if (ok) "로그인 성공" else "로그인 실패: ${res.code()}")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "login error", e)
                _state.update { it.copy(loading = false) }
                _events.tryEmit("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    fun signup(req: UserSignupRequest) {
        if (isOfflineMode) { _events.tryEmit("로컬 회원가입 성공"); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(loading = true) }
                val ok = api.signup(req).isSuccessful
                _state.update { it.copy(loading = false) }
                _events.tryEmit(if (ok) "회원가입 성공" else "회원가입 실패")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signup error", e)
                _state.update { it.copy(loading = false) }
                _events.tryEmit("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenStore.clear()
            _state.update { it.copy(loading = false, isLoggedIn = false) }
            _events.tryEmit("로그아웃 완료")
        }
    }

    fun isLoggedIn(): Boolean = tokenStore.current().access != null
}
