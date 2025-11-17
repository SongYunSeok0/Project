package com.myrhythm.common

import android.app.Application
import android.util.Log
import com.core.push.PushManager      // ✅ 추가
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyRhythmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRhythm Application started.")

        // ✅ 앱 시작 시 FCM 토큰 로그 찍기
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PushManager.getToken()
            } catch (e: Exception) {
                Log.e("FCM", "Token fetch failed: ${e.message}")
            }
        }
    }
}
