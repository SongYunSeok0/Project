package com.myrhythm.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.auth.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1127 자동로그인 적용
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val authPrefs: AuthPreferencesDataSource
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    // 1127 자동로그인 적용 - 3초 스플래시+자동로그인 여부 체크
    fun checkAutoLogin() {
        viewModelScope.launch {
            Log.d("SplashViewModel", "🔐 자동 로그인 체크 시작")

            // 자동로그인 설정 확인용 로그
            val autoLoginEnabled = authPrefs.isAutoLoginEnabled()
            Log.d("SplashViewModel", "자동로그인 설정: $autoLoginEnabled")
            if (!autoLoginEnabled) {
                Log.d("SplashViewModel", "⏸️ 자동 로그인 비활성화 → Login으로 이동")
                _state.value = SplashState.GoLogin
                return@launch
            }


            val tokens = tokenStore.tokens.first()
            val hasToken = !tokens.access.isNullOrBlank()

            _state.value = if (hasToken) {
                Log.d("SplashViewModel", "✅ 자동 로그인 성공 → Home으로 이동")
                SplashState.GoMain        // 자동로그인 성공
            } else {
                SplashState.GoLogin
            }
        }
    }
}