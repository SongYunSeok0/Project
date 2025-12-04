package com.myrhythm.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.myrhythm.R

class AlarmActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        // 1. 잠금화면 위로 띄우기 & 화면 켜기 설정
        turnScreenOnAndKeyguard()

        // 2. 데이터 세팅
        val title = intent.getStringExtra("title") ?: "약 드실 시간이에요!"
        val body = intent.getStringExtra("body") ?: "복약 시간입니다"

        findViewById<TextView>(R.id.tv_alarm_title).text = title
        findViewById<TextView>(R.id.tv_alarm_message).text = body

        // 3. 소리 재생
        playAlarmSound()

        // 4. 알람 종료 버튼 (누가 와도 누를 수 있음)
        findViewById<Button>(R.id.btn_stop_alarm).setOnClickListener {
            stopAlarm()
        }
    }

    private fun turnScreenOnAndKeyguard() {
        // 안드로이드 8.1 (O_MR1) 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true) // 잠금화면 위로 보여짐 (중요)
            setTurnScreenOn(true)   // 화면을 켬
        }
        // 그 이하 버전
        else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // [공통] 알람이 울리는 동안 화면이 꺼지지 않게 유지 (FLAGS_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            // 소리가 너무 작으면 TYPE_RINGTONE이나 TYPE_NOTIFICATION으로 변경 고려
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()

        // 안드로이드 5.0 이상에서는 finishAndRemoveTask()가 더 깔끔하게 앱을 닫음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}