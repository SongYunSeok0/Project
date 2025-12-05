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
        Log.e(tag, "FCM 메시지 수신됨!")
        Log.e(tag, "FCM 데이터 전체: ${msg.data}")
        Log.e(tag, "FCM notification: ${msg.notification}")
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

                if (planId.isNotEmpty()) {
                    Log.i(tag, "복약 알람 처리 - planId: $planId")
                    sendFullScreenAlarm(title, body, planId, false, msg.data)
                } else {
                    Log.i(tag, "planId 없음 - 일반 알림으로 전환")
                    sendNormalNotification(title, body)
                }
            }

            // 풀스크린 알림 - 보호자 미복용 알림
            "missed_alarm" -> {
                val planId = msg.data["plan_id"] ?: ""

                Log.e(tag, "🚨 missed_alarm 수신! planId=$planId")

                // 🔥 planId가 없어도 보호자 화면은 표시해야 함
                sendFullScreenAlarm(title, body, planId, true, msg.data)
            }

            // 일반 알림
            "login_success", "notice", "NORMAL" -> {
                sendNormalNotification(title, body)
            }

            else -> {
                Log.w(tag, "알 수 없는 타입: $messageType, 일반 알림 처리")
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
        dataMap: Map<String, String>
    ) {
        Log.e(tag, "========================================")
        Log.e(tag, "🔥 풀스크린 알람 생성 시작!")
        Log.e(tag, "planId: $planId")
        Log.e(tag, "isGuardian: $isGuardian")
        Log.e(tag, "dataMap: $dataMap")
        Log.e(tag, "========================================")

        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            // 🔥 새 태스크로 시작 + 기존 태스크 클리어
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // 1. 필수 데이터
            val planIdLong = planId.toLongOrNull() ?: 0L
            putExtra("PLAN_ID", planIdLong)
            putExtra("plan_id", planIdLong) // 둘 다 넣기

            // 2. 타입 지정
            if (isGuardian) {
                putExtra("type", "missed_alarm")
                putExtra("is_guardian", "true")
            } else {
                putExtra("type", "ALARM")
            }

            // 3. 🔥 서버에서 받은 모든 데이터 추가
            for ((key, value) in dataMap) {
                putExtra(key, value)
                Log.d(tag, "Intent에 추가: $key = $value")
            }
        }

        // 🔥 고유한 requestCode 사용 (보호자/환자 구분)
        val requestCode = if (isGuardian) {
            System.currentTimeMillis().toInt()
        } else {
            planId.hashCode()
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
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

        val notificationId = if (isGuardian) {
            System.currentTimeMillis().toInt()
        } else {
            planId.hashCode()
        }

        notificationManager.notify(notificationId, notification)

        Log.e(tag, "🔥 풀스크린 알람 notify 완료! (notificationId=$notificationId)")

        // 🔥 추가: 바로 Activity 실행 시도 (앱이 포그라운드에 있을 때 대비)
        try {
            fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(fullScreenIntent)
            Log.e(tag, "🔥 AlarmActivity 직접 실행 시도 완료!")
        } catch (e: Exception) {
            Log.e(tag, "🔥 AlarmActivity 직접 실행 실패: ${e.message}")
        }
    }

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