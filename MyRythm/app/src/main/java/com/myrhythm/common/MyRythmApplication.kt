package com.myrhythm.common

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk

class MyRythmApplication  : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRythm Application started.")
        
        // Kakao SDK 초기화            // 네이티브앱키는 추후 로컬프로퍼티에 값 넣고 이그노어
        KakaoSdk.init(this, "cc55afc291b3623100865916610483b7")

    }
}