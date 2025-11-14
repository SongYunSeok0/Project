package com.myrhythm.common

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import com.myrhythm.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyRhythmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRhythm Application started.")

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.i("FCM", "current token = $token")
                } else {
                    Log.w("FCM", "getToken failed", task.exception)
                }
            }
    }
}
