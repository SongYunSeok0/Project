package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.network.api.UserApi
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.UserSignupRequest
import com.core.auth.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val isOfflineMode = false

    data class UiState(
        val loading: Boolean = false,
        val message: String? = null,
        val isLoggedIn: Boolean = false
    )

    private val _state = MutableStateFlow(
        UiState(isLoggedIn = tokenStore.current().access != null)
    )
    val state: StateFlow<UiState> = _state

    fun login(id: String, pw: String, onResult: (Boolean, String) -> Unit) {
        if (isOfflineMode) {
            viewModelScope.launch { tokenStore.set("offline-access", "offline-refresh") }
            _state.value = UiState(loading = false, isLoggedIn = true, message = "오프라인")
            onResult(true, "로컬 로그인 성공")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.emit(_state.value.copy(loading = true, message = null))
                val res = api.login(UserLoginRequest(id, pw))
                val body = res.body()
                val ok = res.isSuccessful && body?.access != null

                if (ok) {
                    tokenStore.set(access = body!!.access, refresh = body.refresh)
                }

                withContext(Dispatchers.Main) {
                    _state.value = UiState(
                        loading = false,
                        isLoggedIn = ok,
                        message = if (ok) "로그인 성공" else "로그인 실패: ${res.code()}"
                    )
                    onResult(ok, _state.value.message ?: "")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "login error", e)
                withContext(Dispatchers.Main) {
                    val msg = "네트워크 오류: ${e.localizedMessage}"
                    _state.value = UiState(loading = false, isLoggedIn = false, message = msg)
                    onResult(false, msg)
                }
            }
        }
    }

    fun signup(req: UserSignupRequest, onResult: (Boolean, String) -> Unit) {
        if (isOfflineMode) {
            onResult(true, "로컬 회원가입 성공")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.emit(_state.value.copy(loading = true, message = null))
                val res = api.signup(req)
                val ok = res.isSuccessful
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        loading = false,
                        message = if (ok) "회원가입 성공" else "회원가입 실패: ${res.code()}"
                    )
                    onResult(ok, _state.value.message ?: "")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signup error", e)
                withContext(Dispatchers.Main) {
                    val msg = "네트워크 오류: ${e.localizedMessage}"
                    _state.value = _state.value.copy(loading = false, message = msg)
                    onResult(false, msg)
                }
            }
        }
    }

    fun logout(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            tokenStore.clear()
            _state.value = UiState(loading = false, isLoggedIn = false, message = "로그아웃 완료")
            onDone?.invoke()
        }
    }

    fun isLoggedIn(): Boolean = tokenStore.current().access != null
}
