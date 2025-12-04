package com.myrhythm.alarm

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
        setContentView(R.layout.activity_alarm_patient)

        // 1. 잠금화면 위로 띄우기 & 화면 켜기 설정
        turnScreenOnAndKeyguard()

        // 🔹 추가로 받을 값들
        val username   = intent.getStringExtra("username") ?: ""          // 사용자 이름
        val label      = intent.getStringExtra("label") ?: ""             // regihistory.label
        val protName   = intent.getStringExtra("prot_name") ?: ""         // 보호자 이름(필요 시)

        // 2. 기존 title/body도 그대로 사용 가능
        val defaultTitle = "약 드실 시간이에요!"
        val defaultBody  = "복약 시간입니다"

        // 🔹 title/body를 username, label로 꾸미기 (원하면 형식 바꾸면 됨)
        val title = intent.getStringExtra("title")
            ?: if (label.isNotBlank() && username.isNotBlank()) {
                "$username 님, '$label' 약 드실 시간이에요!"
            } else {
                defaultTitle
            }

        val body = intent.getStringExtra("body")
            ?: if (protName.isNotBlank()) {
                "복약 시간입니다. 보호자 $protName 님께도 알림이 전송됩니다."
            } else {
                defaultBody
            }

        findViewById<TextView>(R.id.tv_alarm_title).text = title
        findViewById<TextView>(R.id.tv_alarm_message).text = body

        // 3. 소리 재생
        playAlarmSound()

        // 4. 알람 종료 버튼
        findViewById<Button>(R.id.btn_stop_alarm).setOnClickListener {
            stopAlarm()
        }
    }

    private fun turnScreenOnAndKeyguard() { /* 기존 그대로 */ }

    private fun playAlarmSound() { /* 기존 그대로 */ }

    private fun stopAlarm() { /* 기존 그대로 */ }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}
