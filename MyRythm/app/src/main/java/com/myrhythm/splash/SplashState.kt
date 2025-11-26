package com.myrhythm.splash

// 1126
sealed class SplashState {
    object Loading : SplashState()
    object GoLogin : SplashState()
    object GoMain : SplashState()
}