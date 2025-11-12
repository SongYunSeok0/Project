package com.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtil {
    private const val CHANNEL_ID = "alert"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = ctx.getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "알림", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    fun show(ctx: Context, title: String, body: String) {
        ensureChannel(ctx) // 채널 IMPORTANCE_HIGH 이미 생성

        val n = NotificationCompat.Builder(ctx, "alert")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // 큰 텍스트
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 소리/진동/라이트
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx)
            .notify(System.currentTimeMillis().toInt(), n)
    }
}
