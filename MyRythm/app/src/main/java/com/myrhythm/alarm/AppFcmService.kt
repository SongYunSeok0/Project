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
import com.shared.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppFcmService : FirebaseMessagingService() {

    private val tag = "FCM_SERVICE"

    override fun onNewToken(token: String) {
        Log.i(tag, "onNewToken: $token")
        // 토큰 서버 전송 로직...
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        // 1. 기본 알림 데이터
        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body = msg.notification?.body ?: msg.data["body"] ?: ""

        // 2. 메시지 타입 확인
        val messageType = msg.data["type"] ?: "NORMAL"

        // 3. ⭐ [핵심] 추가 데이터 추출 (서버에서 보낸 키값과 일치해야 함)
        val planId = msg.data["plan_id"] ?: ""
        val username = msg.data["user_name"] ?: ""        // 예: 홍길동
        val medicineLabel = msg.data["med_name"] ?: ""    // 예: 타이레놀
        val takenAtTime = msg.data["taken_at"] ?: ""      // 예: 09:00
        val mealTime = msg.data["meal_time"] ?: ""        // 예: 식후 30분
        val note = msg.data["note"] ?: ""                 // 예: 물과 함께
        val isGuardian = msg.data["is_guardian"] ?: "false"

        Log.i(tag, "onMessageReceived: type=$messageType, planId=$planId")

        // 4. 타입에 따라 분기
        when (messageType) {
            "ALARM", "med_alarm" -> {
                // 환자 정시 알림 (전체 화면)
                sendFullScreenAlarm(
                    title, body,
                    planId, username, medicineLabel, takenAtTime, mealTime, note,
                    isGuardian
                )
            }
            "missed_alarm" -> {
                // 보호자 미복용 알림 (전체 화면)
                sendFullScreenAlarm(
                    title, body,
                    planId, username, medicineLabel, takenAtTime, mealTime, note,
                    "true" // 보호자 강제 지정
                )
            }
            else -> sendNotification(title, body)
        }
    }

    /**
     * ⭐ 파라미터 대폭 추가: 화면 구성에 필요한 데이터들
     */
    private fun sendFullScreenAlarm(
        title: String,
        messageBody: String,
        planId: String,
        username: String,
        medicineLabel: String,
        takenAtTime: String,
        mealTime: String,
        note: String,
        isGuardianString: String
    ) {
        // AlarmActivity를 띄우는 Intent 생성
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // 기본 정보
            putExtra("title", title)
            putExtra("body", messageBody)

            // ⭐ [핵심] 화면 표시에 필요한 상세 데이터 전달
            putExtra("plan_id", planId)
            putExtra("user_name", username)
            putExtra("med_name", medicineLabel)
            putExtra("taken_at", takenAtTime)
            putExtra("meal_time", mealTime)
            putExtra("note", note)
            putExtra("is_guardian", isGuardianString)

            if (isGuardianString == "true") {
                putExtra("type", "missed_alarm")
            }
        }

        // PendingIntent 생성 (planId.hashCode()로 구분하여 덮어쓰기 방지)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            planId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "alarm_channel"
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true) // 잠금화면 위로 표시
            .setContentIntent(fullScreenPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "복약 알람",
                NotificationManager.IMPORTANCE_HIGH
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

    private fun sendNotification(title: String, messageBody: String) {
        // 일반 알림 로직 (기존 유지)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel_id"
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
            val channel = NotificationChannel(channelId, "기본 알림", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}