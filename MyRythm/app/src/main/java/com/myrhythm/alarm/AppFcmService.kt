package com.myrhythm.alarm

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

        PushManager.fcmToken = token
        FcmTokenStore(this).saveToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                registerFcmTokenUseCase(token)
            }.onFailure {
                Log.e(tag, "서버에 토큰 등록 실패", it)
            }
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body = msg.notification?.body ?: msg.data["body"] ?: ""

        // ⭐ 메시지 타입 확인 (서버에서 "type": "ALARM" 보내야 함)
        val messageType = msg.data["type"] ?: "NORMAL"

        Log.i(tag, "onMessageReceived title=$title body=$body type=$messageType")


        // 타입에 따라 다른 알림 표시
        when (messageType) {
            "ALARM" -> sendFullScreenAlarm(title, body)  // 전체 화면 알람
            else -> sendNotification(title, body)        // 일반 알림
        }
    }

    /**
     * ⭐ 새로 추가: 전체 화면 알람 (화면 깨우기 + 잠금화면 위에 표시)
     */
    private fun sendFullScreenAlarm(title: String, messageBody: String) {
        // AlarmActivity를 띄우는 Intent
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", title)
            putExtra("body", messageBody)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "alarm_channel"  // 알람 전용 채널
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)  // 최고 우선순위
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)  // 👈 핵심!
            .setContentIntent(fullScreenPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알람 전용 채널 생성 (중요도 높음)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "복약 알람",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "약 복용 시간 알람"
                enableVibration(true)
                setSound(alarmSoundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * 기존 일반 알림 (그대로 유지)
     */
    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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