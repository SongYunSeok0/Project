package com.myrhythm.common

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import com.myrhythm.BuildConfig
import com.data.BuildConfig as DataBuildConfig   // ★ data 모듈의 BASE_URL 씀

import com.data.network.api.ChatbotApi
import com.data.network.dto.chatbot.ChatRequest   // 실제 패키지명에 맞게 수정

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.core.push.PushManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyRhythmApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            override fun onCreate() {
            super.onCreate()
            Log.d("AppApplication", "MyRhythm Application started.")

            // Kakao SDK 초기화
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("FCM", "getToken 실패", task.exception)
                            return@addOnCompleteListener
                        }
                        val token = task.result
                        Log.i("FCM", "current token = $token")
                        PushManager.fcmToken = token
                    }
            }
}
