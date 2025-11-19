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
class MyRhythmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRhythm Application started.")

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        warmupChatbot()
    }

    private fun warmupChatbot() {
        appScope.launch {
            try {
                // Moshi & Retrofit 인스턴스 로컬로 생성
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(DataBuildConfig.BACKEND_BASE_URL) // "http://10.0.2.2:8000/api/"
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()

                val api = retrofit.create(ChatbotApi::class.java)

                // 서버에 가벼운 질문 한 번 날려서 RAG/LLM 깨우기
                val req = ChatRequest(
                    question = "워밍업",   // 실제 필드명에 맞게 수정
                )

                // 결과는 버림. 실패해도 사용자에겐 안 보임.
                api.askDrugRag(req)

                Log.d("AppApplication", "Chatbot warmup success")
            } catch (e: Exception) {
                Log.w("AppApplication", "Chatbot warmup failed: ${e.message}")
            }
        }
        // ✅ FCM 현재 토큰 가져와서 PushManager에 저장
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