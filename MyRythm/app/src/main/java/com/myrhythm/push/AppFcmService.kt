// app/src/main/java/com/myrhythm/push/AppFcmService.kt
package com.myrhythm.push

import android.util.Log
import com.data.core.push.NotificationUtil
import com.data.core.push.PushManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.i("FCM", "new token = $token")
        PushManager.fcmToken = token
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""

        Log.i("FCM", "onMessageReceived title=$title body=$body")

        NotificationUtil.show(this, title, body)
    }
}
