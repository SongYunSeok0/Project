package com.auth.viewmodel

import android.util.Log
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
        val errorMessage: String? = null,
        val isInitializing: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _autoLoginEnabled = MutableStateFlow(false)
    val autoLoginEnabled: StateFlow<Boolean> = _autoLoginEnabled.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() = viewModelScope.launch {
        Log.e("LoginViewModel", "🔍 ========== checkLoginStatus() 시작 ==========")

        _autoLoginEnabled.value = authPrefs.isAutoLoginEnabled()

        val currentTokens = tokenStore.current()
        val token = currentTokens.access

        Log.e("LoginViewModel", "토큰 확인: ${token?.take(50)}...")
        Log.e("LoginViewModel", "토큰 null? ${token == null}, 비어있음? ${token?.isBlank()}")

        if (!token.isNullOrBlank()) {
            Log.e("LoginViewModel", "토큰 있음! userId 추출 시도")
            val userId = JwtUtils.extractUserId(token)
            Log.e("LoginViewModel", "추출된 userId: $userId")

            if (userId != null) {
                Log.e("LoginViewModel", "✅ userId 추출 성공! isLoggedIn = true 설정")
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        userId = userId,
                        isInitializing = false
                    )
                }
                Log.e("LoginViewModel", "✅ 초기화 시 로그인 상태 설정: userId=$userId")
                Log.e("LoginViewModel", "현재 uiState: ${_uiState.value}")
            } else {
                Log.e("LoginViewModel", "❌ userId 추출 실패! 토큰 삭제")
                tokenStore.clear()
                _uiState.update { it.copy(isInitializing = false) }
            }
        } else {
            Log.e("LoginViewModel", "❌ 토큰 없음")
            _uiState.update { it.copy(isInitializing = false) }
        }

        Log.e("LoginViewModel", "🔍 checkLoginStatus() 완료, 최종 isLoggedIn: ${_uiState.value.isLoggedIn}")
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

        Log.e("LoginViewModel", "🔐 로그인 시도 시작")

        when (val result = loginUseCase(email, password, _autoLoginEnabled.value)) {
            is com.domain.model.ApiResult.Success -> {
                Log.e("LoginViewModel", "✅ LoginUseCase 성공")
                Log.e("LoginViewModel", "받은 토큰: access=${result.data.access}")
                Log.e("LoginViewModel", "받은 토큰: refresh=${result.data.refresh}")

                authPrefs.setAutoLoginEnabled(_autoLoginEnabled.value)
                val uid = JwtUtils.extractUserId(result.data.access) ?: ""

                Log.e("LoginViewModel", "추출된 userId: $uid")

                _uiState.update {
                    it.copy(
                        loading = false,
                        isLoggedIn = true,
                        userId = uid,
                        errorMessage = null,
                        isInitializing = false
                    )
                }

                Log.e("LoginViewModel", "✅ uiState 업데이트 완료: ${_uiState.value}")

                // 🔥 토큰 저장 확인
                val currentTokens = tokenStore.current()
                Log.e("LoginViewModel", "========================================")
                Log.e("LoginViewModel", "📦 TokenStore 확인:")
                Log.e("LoginViewModel", "  - access 있음: ${currentTokens.access != null}")
                Log.e("LoginViewModel", "  - access 값: ${currentTokens.access?.take(50)}...")
                Log.e("LoginViewModel", "  - refresh 있음: ${currentTokens.refresh != null}")
                Log.e("LoginViewModel", "========================================")
            }
            is com.domain.model.ApiResult.Failure -> {
                Log.e("LoginViewModel", "❌ LoginUseCase 실패: ${result.error}")
                val message = mapErrorToMessage(result.error)
                _uiState.update {
                    it.copy(loading = false, errorMessage = message)
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        Log.e("LoginViewModel", "🚪 logout() 시작")

        // 🔥 로그아웃 전 토큰 확인
        val beforeTokens = tokenStore.current()
        Log.e("LoginViewModel", "========================================")
        Log.e("LoginViewModel", "📦 로그아웃 전 TokenStore:")
        Log.e("LoginViewModel", "  - access 있음: ${beforeTokens.access != null}")
        Log.e("LoginViewModel", "  - refresh 있음: ${beforeTokens.refresh != null}")
        Log.e("LoginViewModel", "========================================")

        // 로그아웃 시도
        when (val result = logoutUseCase()) {
            is com.domain.model.ApiResult.Success -> {
                Log.e("LoginViewModel", "✅ LogoutUseCase 성공")
            }
            is com.domain.model.ApiResult.Failure -> {
                Log.e("LoginViewModel", "⚠️ LogoutUseCase 서버 요청 실패: ${result.error}")
                Log.e("LoginViewModel", "하지만 로컬 데이터는 삭제됨")
            }
        }

        // 결과와 관계없이 UI 상태 업데이트
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                userId = null,
                errorMessage = null,
                isInitializing = false
            )
        }
        Log.e("LoginViewModel", "✅ 로그아웃 상태로 변경 완료")

        _autoLoginEnabled.value = false
        authPrefs.setAutoLoginEnabled(false)

        // 🔥 로그아웃 후 토큰 확인
        val afterTokens = tokenStore.current()
        Log.e("LoginViewModel", "========================================")
        Log.e("LoginViewModel", "📦 로그아웃 후 TokenStore:")
        Log.e("LoginViewModel", "  - access 있음: ${afterTokens.access != null}")
        Log.e("LoginViewModel", "  - refresh 있음: ${afterTokens.refresh != null}")
        Log.e("LoginViewModel", "========================================")
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