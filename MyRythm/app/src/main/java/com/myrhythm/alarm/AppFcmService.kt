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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.myrhythm.MainActivity
import com.shared.R // R 파일 경로 수정 (프로젝트 패키지에 맞춤)
import dagger.hilt.android.AndroidEntryPoint
// import com.data.core.push.FcmTokenStore
// import com.data.core.push.PushManager
// import com.domain.usecase.push.RegisterFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppFcmService : FirebaseMessagingService() {

    private val tag = "FCM_SERVICE"

    // Hilt나 UseCase가 없다면 주석 처리 후 사용하세요
    // @Inject
    // lateinit var registerFcmTokenUseCase: RegisterFcmTokenUseCase

    override fun onNewToken(token: String) {
        Log.i(tag, "onNewToken: $token")

        // 토큰 저장 및 서버 전송 로직
        // PushManager.fcmToken = token
        // FcmTokenStore(this).saveToken(token)

        /*
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                registerFcmTokenUseCase(token)
            }.onFailure {
                Log.e(tag, "서버에 토큰 등록 실패", it)
            }
        }
        */
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body = msg.notification?.body ?: msg.data["body"] ?: ""

        // ⭐ 메시지 타입 확인 (서버에서 "type": "ALARM" 보내야 함)
        val messageType = msg.data["type"] ?: "NORMAL"

        Log.i(tag, "onMessageReceived title=$title body=$body type=$messageType")

        // 타입에 따라 다른 알림 표시
        when (messageType) {
            "ALARM", "med_alarm" -> sendFullScreenAlarm(title, body)  // 전체 화면 알람
            else -> sendNotification(title, body)                     // 일반 알림
        }
    }

    /**
     * ⭐ [핵심] 전체 화면 알람 (화면 깨우기 + 잠금화면 위에 표시)
     */
    private fun sendFullScreenAlarm(title: String, messageBody: String) {
        // AlarmActivity를 띄우는 Intent
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            // 이 플래그들이 중요합니다
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", title)
            putExtra("body", messageBody)
        }

        // Android 12(S) 이상은 FLAG_IMMUTABLE 필수
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "alarm_channel"  // 알람 전용 채널
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 아이콘이 없으면 ic_launcher 등으로 변경
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)  // 중요도 MAX (헤드업 알림 등)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)  // ⭐ 여기가 핵심: 잠금화면 위로 띄움
            .setContentIntent(fullScreenPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오(8.0) 이상 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "복약 알람",
                NotificationManager.IMPORTANCE_HIGH // 소리 울리고 배너 뜸
            ).apply {
                description = "약 복용 시간 알람"
                enableVibration(true)
                setSound(alarmSoundUri, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * 기존 일반 알림
     */
    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel_id" // strings.xml 리소스 사용 권장
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
                "기본 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}