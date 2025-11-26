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

// 1126
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val authPrefs: AuthPreferencesDataSource
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state


    init {
        checkAutoLogin()
    }


    /** 3초 후 스플래시 화면 → 자동로그인 체크 */
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
            //토큰확인용 로그만 추가
            Log.d("SplashViewModel", "토큰 존재 여부: $hasToken (access=${tokens.access?.take(20)}...)")

            _state.value = if (hasToken) {
                Log.d("SplashViewModel", "✅ 자동 로그인 성공 → Home으로 이동")
                SplashState.GoMain        // 자동로그인
            } else {
                Log.d("SplashViewModel", "❌ 토큰 없음 → Login으로 이동")
                SplashState.GoLogin       // 로그인 화면으로
            }
        }
    }
}