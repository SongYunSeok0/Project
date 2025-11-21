// app/src/main/java/com/myrhythm/push/AppFcmService.kt
package com.myrhythm.push

import android.Manifest
import android.util.Log
import com.data.core.push.NotificationUtil
import com.data.core.push.PushManager
import androidx.annotation.RequiresPermission
import com.data.core.push.FcmTokenStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFcmService : FirebaseMessagingService() {

    private val tag = "FCM_SERVICE"

    override fun onNewToken(token: String) {
        Log.i(tag, "onNewToken: $token")

        //처음만 토큰 가져와서 저장
        FcmTokenStore(this).saveToken(token)
        //메모리에도 저장
        PushManager.fcmToken = token
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""

        Log.i("FCM", "onMessageReceived title=$title body=$body")

        NotificationUtil.show(this, title, body)
    }
}
