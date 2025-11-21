package com.data.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class AppFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.i("FCM", "new token = $token")  // 서버 없으면 일단 로그로 확인
        // TODO: 서버 있으면 여기서 업로드
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Log.w("FCM", "POST_NOTIFICATIONS 미허용 → 알림 표시 스킵")
                return
            }
        }

        @SuppressLint("MissingPermission")
        NotificationUtil.show(this, title, body)
    }
}
