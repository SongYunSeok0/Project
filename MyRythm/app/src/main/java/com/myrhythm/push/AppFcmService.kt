package com.myrhythm.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // 여기서 새 토큰 수신
        Log.i("FCM", "new token = $token")

        // TODO: 나중에 서버로 전송하거나 core 쪽 토큰 저장소에 넘기기
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "알림"
        val body  = message.notification?.body  ?: message.data["body"]  ?: ""

        Log.i("FCM", "onMessageReceived title=$title body=$body")

        // 일단 간단히 시스템 알림 띄우기 (아래 2번 Notification 코드 참고해서 호출)
        NotificationHelper.showNotification(this, title, body)
    }
}
