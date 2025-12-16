package com.myrhythm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
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
        // 1. CPU 깨우기 (매우 중요: Doze 모드 방지)
        acquireWakeLock(this)

        Log.e(tag, "========================================")
        Log.e(tag, "📨 FCM 메시지 수신됨")
        Log.e(tag, "Data: ${msg.data}")
        Log.e(tag, "========================================")

        val title = msg.data["title"] ?: msg.notification?.title ?: "알림"
        val body = msg.data["body"] ?: msg.notification?.body ?: ""
        val messageType = msg.data["type"] ?: "NORMAL"

        when (messageType) {
            // 풀스크린 알림 - 환자 복약 알림
            "ALARM", "med_alarm" -> {
                // ⭕ 수정: plan_id가 없으면 plan_ids도 찾아보게 변경
                val planId = msg.data["plan_id"] ?: msg.data["plan_ids"] ?: ""

                if (planId.isNotEmpty()) {
                    Log.e(tag, "✅ ALARM 모드 진입: ID=$planId") // 확인용 로그
                    sendFullScreenAlarm(title, body, planId, false, msg.data)
                } else {
                    Log.w(tag, "⚠️ ID 없음. 일반 알림 처리")
                    sendNormalNotification(title, body)
                }
            }

            // 풀스크린 알림 - 보호자 미복용 알림
            "missed_alarm" -> {
                val planId = msg.data["plan_id"] ?: msg.data["plan_ids"] ?: ""
                // 보호자는 planId 없어도 화면 띄움
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

    private fun sendFullScreenAlarm(
        title: String,
        messageBody: String,
        planId: String,
        isGuardian: Boolean,
        dataMap: Map<String, String>
    ) {
        // 1. 알람 화면 Intent 설정
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // 필수 데이터
            val planIdLong = planId.toLongOrNull() ?: 0L
            putExtra("PLAN_ID", planIdLong)
            putExtra("plan_id", planIdLong)

            // 타입 지정
            if (isGuardian) {
                putExtra("type", "missed_alarm")
                putExtra("is_guardian", "true")
            } else {
                putExtra("type", "ALARM")
            }

            // 전체 데이터 덤프
            for ((key, value) in dataMap) {
                putExtra(key, value)
            }
        }

        // 2. PendingIntent (고유 ID 사용)
        val requestCode = if (isGuardian) System.currentTimeMillis().toInt() else planId.hashCode()
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ⭐ [중요] 채널 ID를 v2로 변경하여 기존 설정(Silent 등)을 초기화시킴
        val channelId = "alarm_channel_high_priority_v3"
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 3. Notification Channel 설정 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 이미 채널이 존재하면 삭제하고 다시 만들거나, 설정을 확인 (여기선 v2라서 새로 생성됨)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM) // ⭐ 용도: 알람
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "복약 중요 알림",
                NotificationManager.IMPORTANCE_HIGH // ⭐ 중요도 HIGH 필수
            ).apply {
                description = "약 복용 시간을 전체 화면으로 알려줍니다."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // 진동 패턴 명시
                setSound(alarmSoundUri, audioAttributes) // ⭐ 오디오 속성 적용
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. Notification Builder
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_MAX) // ⭐ 우선순위 MAX
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSoundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true) // ⭐ 핵심: 풀스크린 인텐트
            .setContentIntent(fullScreenPendingIntent) // 클릭 시에도 이동
            .build()

        val notificationId = if (isGuardian) System.currentTimeMillis().toInt() else planId.hashCode()
        notificationManager.notify(notificationId, notification)

        Log.e(tag, "🔥 Notify 완료 (ID=$notificationId). 화면이 켜져야 합니다.")

        // 5. [보조 수단] 포그라운드 상태 등에서 즉시 실행 시도
        try {
            fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            // 백그라운드에서는 실패할 수 있음 (정상)
            Log.w(tag, "직접 startActivity 실패 (백그라운드 제약 가능성): ${e.message}")
        }
    }

    private fun sendNormalNotification(title: String, messageBody: String) {
        val channelId = "default_channel_v1"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "일반 알림", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(defaultSoundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // 화면/CPU 깨우기 헬퍼 함수
    private fun acquireWakeLock(context: Context) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MyRhythm:FCMWakeLock"
            )
            wakeLock.acquire(3000) // 3초간 유지
        } catch (e: Exception) {
            Log.e(tag, "WakeLock 획득 실패: ${e.message}")
        }
    }
}