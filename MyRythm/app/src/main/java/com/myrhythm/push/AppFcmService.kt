package com.myrhythm.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.myrhythm.MainActivity
import com.shared.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppFcmService : FirebaseMessagingService() {

    private val tag = "FCM_SERVICE"

    @Inject
    lateinit var registerFcmTokenUseCase: RegisterFcmTokenUseCase

    override fun onNewToken(token: String) {
        Log.i(tag, "onNewToken: $token")

        // 1. 메모리 및 로컬 저장 (기존 로직 유지)
        PushManager.fcmToken = token
        FcmTokenStore(this).saveToken(token)

        // ⭐ 2. [추가됨] 서버(Django)에도 갱신된 토큰 전송 (필수!)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                registerFcmTokenUseCase(token)
            }.onFailure {
                Log.e(tag, "서버에 토큰 등록 실패", it)
            }
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        // 알림 내용 추출
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""

        Log.i(tag, "onMessageReceived title=$title body=$body")

        // 알림 표시 함수 호출
        sendNotification(title, body)
    }

    private fun sendNotification(title: String, messageBody: String) {
        // 알림 클릭 시 메인 액티비티로 이동
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 아이콘 리소스 확인 필요
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 이상 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "기본 알림 채널",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}