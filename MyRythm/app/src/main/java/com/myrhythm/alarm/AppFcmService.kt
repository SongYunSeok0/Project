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

        val title = msg.notification?.title ?: msg.data["title"] ?: "알림"
        val body = msg.notification?.body ?: msg.data["body"] ?: ""
        val messageType = msg.data["type"] ?: "NORMAL"

        Log.i(tag, "FCM 수신: type=$messageType, title=$title")

        when (messageType) {
            // 풀스크린 알림 - 환자 복약 알림
            "ALARM", "med_alarm" -> {
                val planId = msg.data["plan_id"] ?: ""

                if (planId.isEmpty()) {
                    Log.e(tag, "planId 누락!")
                    return
                }

                Log.i(tag, "복약 알람 처리 - planId: $planId")
                sendFullScreenAlarm(title, body, planId, isGuardian = false)
            }

            // 풀스크린 알림 - 보호자 미복용 알림
            "missed_alarm" -> {
                val planId = msg.data["plan_id"] ?: ""

                if (planId.isEmpty()) {
                    Log.e(tag, "planId 누락!")
                    return
                }

                Log.i(tag, "미복용 알람 처리 - planId: $planId")
                sendFullScreenAlarm(title, body, planId, isGuardian = true)
            }

            // 일반 알림 - 로그인, 공지사항 등
            "login_success", "notice", "NORMAL" -> {
                sendNormalNotification(title, body)
            }

            // 기타 타입은 일반 알림으로 처리
            else -> {
                sendNormalNotification(title, body)
            }
        }
    }

    /**
     * 풀스크린 알림 (복약 알림)
     * - 화면 자동으로 켜짐
     * - AlarmActivity 실행
     * - 알람음 재생
     */
    private fun sendFullScreenAlarm(
        title: String,
        messageBody: String,
        planId: String,
        isGuardian: Boolean
    ) {
        Log.i(tag, "풀스크린 알람 생성 - planId: $planId, 보호자: $isGuardian")

        // AlarmActivity를 실행할 Intent
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            // ⭐ 대문자로 통일!
            putExtra("PLAN_ID", planId.toLongOrNull() ?: 0L)

            if (isGuardian) {
                putExtra("type", "missed_alarm")
            }
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            planId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "alarm_channel"
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true) // 핵심: 잠금화면 위로 표시
            .setContentIntent(fullScreenPendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O 이상: 알림 채널 생성
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
        Log.i(tag, "풀스크린 알람 전송 완료")
    }

    /**
     * 일반 알림 (작업표시줄)
     * - 화면 안 켜짐
     * - MainActivity로 이동
     * - 일반 알림음
     */
    private fun sendNormalNotification(title: String, messageBody: String) {
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

        // Android O 이상: 알림 채널 생성
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