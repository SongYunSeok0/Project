package com.myrhythm

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.messaging.FirebaseMessaging
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.myrhythm.ui.theme.MyRhythmTheme
import com.myrythm.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FCM 토큰 초기화
        initFcmToken()

        // 알림 권한 요청
        askNotificationPermission()

        // 걸음수 센서 권한 요청
        askActivityRecognitionPermission()

        // Compose 앱 시작
        setContent {
            MyRhythmTheme {
                AppRoot()
            }
        }
    }

    /**
     * ACTIVITY_RECOGNITION 권한 요청
     */
    private fun askActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val perm = android.Manifest.permission.ACTIVITY_RECOGNITION
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(perm), 2001)
            }
        }
    }

    /**
     * 알림 권한 요청 (Android 13 이상)
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(perm), 1001)
            }
        }
    }

    /**
     * FCM Token 초기화
     */
    private fun initFcmToken() {
        val store = FcmTokenStore(this)

        // 로컬에 저장된 토큰 우선 사용
        val localToken = store.getToken()
        if (localToken != null) {
            Log.i(tag, "FCM local token = $localToken")
            PushManager.fcmToken = localToken
            return
        }

        // Firebase에서 신규 토큰 발급
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(tag, "getToken 실패", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.i(tag, "FCM firebase token = $token")

                PushManager.fcmToken = token
                store.saveToken(token)
            }
    }
}
