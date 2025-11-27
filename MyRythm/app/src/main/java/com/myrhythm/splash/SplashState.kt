package com.myrhythm.splash

// 1127 자동로그인 적용
sealed class SplashState {
    object Loading : SplashState()
    object GoLogin : SplashState()
    object GoMain : SplashState()
}