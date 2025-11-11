package com.myrhythm.common

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.myrhythm.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyRythmApplication  : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRythm Application started.")

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

    }
}