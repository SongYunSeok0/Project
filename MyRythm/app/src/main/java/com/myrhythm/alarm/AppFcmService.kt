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

@AndroidEntryPoint
class AppFcmService : FirebaseMessagingService() {

    private val tag = "FCM_SERVICE"

    override fun onNewToken(token: String) {
        Log.i(tag, "onNewToken: $token")
        // TODO: 토큰 서버 전송 로직
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        // ⭐ 디버깅용 로그
        Log.e(tag, "========================================")
        Log.e(tag, "FCM 데이터 전체: ${msg.data}")
        Log.e(tag, "========================================")

        // data payload 우선 사용 (notification 필드가 없으므로 data가 필수)
        val title = msg.data["title"] ?: msg.notification?.title ?: "알림"
        val body = msg.data["body"] ?: msg.notification?.body ?: ""
        val messageType = msg.data["type"] ?: "NORMAL"

        Log.i(tag, "FCM 수신: type=$messageType, title=$title")

        when (messageType) {
            // 풀스크린 알림 - 환자 복약 알림
            "ALARM", "med_alarm" -> {
                val planId = msg.data["plan_id"] ?: ""
                if (planId.isEmpty()) return

                Log.i(tag, "복약 알람 처리 - planId: $planId")
                // ⭐ msg.data 전체를 넘김
                sendFullScreenAlarm(title, body, planId, false, msg.data)
            }

            // 풀스크린 알림 - 보호자 미복용 알림
            "missed_alarm" -> {
                val planId = msg.data["plan_id"] ?: ""
                if (planId.isEmpty()) return

                Log.i(tag, "미복용 알람 처리 - planId: $planId")
                // ⭐ msg.data 전체를 넘김
                sendFullScreenAlarm(title, body, planId, true, msg.data)
            }

            // 일반 알림
            "login_success", "notice", "NORMAL" -> {
                sendNormalNotification(title, body)
            }

            else -> {
                sendNormalNotification(title, body)
            }
        }
    }

    /**
     * 풀스크린 알림 (복약 알림)
     * ⭐ dataMap 파라미터 추가: 서버에서 받은 모든 텍스트 데이터를 Intent에 넣기 위함
     */
    private fun sendFullScreenAlarm(
        title: String,
        messageBody: String,
        planId: String,
        isGuardian: Boolean,
        dataMap: Map<String, String> // 추가된 파라미터
    ) {
        Log.i(tag, "풀스크린 알람 생성 - planId: $planId, 보호자: $isGuardian")

        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            // 1. 필수 ID 넣기
            putExtra("PLAN_ID", planId.toLongOrNull() ?: 0L)

            // 2. 타입 지정
            if (isGuardian) {
                putExtra("type", "missed_alarm")
            }

            // 3. ⭐ [핵심 수정] 서버에서 받은 나머지 데이터(user_name, med_name 등)를 모두 Intent에 넣음
            for ((key, value) in dataMap) {
                putExtra(key, value)
            }
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            planId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "alarm_channel"
        // 알림음 설정 (TYPE_ALARM 권장)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true) // 잠금화면 위로 즉시 실행
            .setContentIntent(fullScreenPendingIntent)
            .build()

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

        notificationManager.notify(planId.hashCode(), notification)
        Log.i(tag, "풀스크린 알람 전송 완료 (데이터 포함됨)")
    }

    private fun sendNormalNotification(title: String, messageBody: String) {
        // ... (기존과 동일)
        Log.i(tag, "일반 알림 생성: title=$title")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "일반 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "로그인, 공지사항 등"
                setSound(defaultSoundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.i(tag, "일반 알림 전송 완료")
    }
}