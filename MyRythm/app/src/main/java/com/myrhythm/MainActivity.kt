package com.myrhythm

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.kakao.sdk.common.util.Utility
import com.myrhythm.ui.theme.MyRhythmTheme
import com.myrythm.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFcmToken()
        askNotificationPermission()
        setContent { AppRoot() }

    }

    private fun initFcmToken() {
        val store = FcmTokenStore(this)

        // 1) 로컬에 저장된 토큰 먼저 확인
        val localToken = store.getToken()
        if (localToken != null) {
            Log.i(tag, "FCM local token = $localToken")
            PushManager.fcmToken = localToken
            return
        }

        //토큰없을경우 Firebase에서 가져오기
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(tag, "getToken 실패", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.i(tag, "FCM firebase token = $token")

                // 메모리 + SharedPreferences 모두 저장
                PushManager.fcmToken = token
                store.saveToken(token)
            }
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(perm), 1001)
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRhythmTheme { AppRoot() }
}


