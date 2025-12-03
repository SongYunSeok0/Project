package com.myrhythm.alarm

import android.app.KeyguardManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.myrhythm.R

class AlarmActivity : AppCompatActivity() {

    private var ringtone: android.media.Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ 레이아웃 설정!
        setContentView(R.layout.activity_alarm)

        // 화면 깨우기 및 잠금화면 위에 표시
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // 잠금 해제 요청
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // FCM에서 받은 데이터 표시
        val title = intent.getStringExtra("title") ?: "약 드실 시간이에요!"
        val body = intent.getStringExtra("body") ?: "복약 시간입니다"

        findViewById<TextView>(R.id.tv_alarm_title).text = title
        findViewById<TextView>(R.id.tv_alarm_message).text = body

        // 알람 소리 재생
        playAlarmSound()

        // 버튼 클릭 이벤트
        findViewById<Button>(R.id.btn_stop_alarm).setOnClickListener {
            ringtone?.stop()
            finish()  // Activity 종료
        }
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}