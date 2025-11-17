package com.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.i("FCM", "new token = $token")  // 서버 없으면 일단 로그로 확인
        // TODO: 서버 있으면 여기서 업로드
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        Log.i("FCM", "from=${msg.from} data=${msg.data} notif=${msg.notification}")
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""
        Log.i("FCM", "show title=$title body=$body")
        NotificationUtil.show(this, title, body)
    }
}
