package com.myrhythm.alarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.myrhythm.R
import com.myrhythm.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private val viewModel: AlarmViewModel by viewModels()
    private var ringtone: Ringtone? = null

    // 데이터
    private var planId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        // 1. 화면 깨우기 & 잠금화면 위로 설정
        turnScreenOnAndKeyguard()

        // 2. Intent 데이터 수신
        val title = intent.getStringExtra("title") ?: "약 드실 시간이에요!"
        val body = intent.getStringExtra("body") ?: "복약 시간입니다"
        // FCM 보낼 때 data에 "plan_id"를 넣어야 함
        val planIdStr = intent.getStringExtra("plan_id") ?: "-1"
        planId = planIdStr.toLongOrNull() ?: -1L

        // 보호자 여부 확인 (FCM data에 "is_guardian"을 true로 보내거나, type으로 구분)
        // 예: type="ALARM" (환자), type="missed_alarm" (보호자)
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = type == "missed_alarm" || intent.getStringExtra("is_guardian") == "true"

        // 3. UI 초기화
        findViewById<TextView>(R.id.tv_alarm_title).text = title
        findViewById<TextView>(R.id.tv_alarm_message).text = body

        setupButtons(isGuardian)

        // 4. 소리 재생
        playAlarmSound()

        // 5. ViewModel 이벤트 관찰 (성공/실패 처리)
        observeViewModel()
    }

    private fun setupButtons(isGuardian: Boolean) {
        val layoutPatient = findViewById<LinearLayout>(R.id.layout_patient_actions)
        val btnGuardian = findViewById<AppCompatButton>(R.id.btn_guardian_confirm)

        if (isGuardian) {
            // [보호자 화면]
            layoutPatient.visibility = View.GONE
            btnGuardian.visibility = View.VISIBLE

            btnGuardian.setOnClickListener {
                stopAlarmAndFinish()
            }
        } else {
            // [환자 화면]
            layoutPatient.visibility = View.VISIBLE
            btnGuardian.visibility = View.GONE

            // (1) 미루기 버튼
            findViewById<AppCompatButton>(R.id.btn_snooze).setOnClickListener {
                if (planId != -1L) {
                    viewModel.snooze(planId) // API 호출
                } else {
                    stopAlarmAndFinish()
                }
            }

            // (2) 복약 완료 버튼
            findViewById<AppCompatButton>(R.id.btn_taken).setOnClickListener {
                if (planId != -1L) {
                    viewModel.markAsTaken(planId) // API 호출
                } else {
                    stopAlarmAndFinish()
                }
            }

            // (3) 닫기 버튼 (API 호출 없이 끔)
            findViewById<TextView>(R.id.btn_close_patient).setOnClickListener {
                stopAlarmAndFinish()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when(event) {
                    is AlarmViewModel.AlarmEvent.Success -> {
                        // 성공하면 알람 끄고 종료
                        stopAlarmAndFinish()
                    }
                    is AlarmViewModel.AlarmEvent.Error -> {
                        Toast.makeText(this@AlarmActivity, event.msg, Toast.LENGTH_SHORT).show()
                        // 에러가 나도 일단 알람은 꺼주는 게 사용자 경험상 좋을 수 있음 (선택사항)
                        stopAlarmAndFinish()
                    }
                }
            }
        }
    }

    private fun turnScreenOnAndKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmAndFinish() {
        ringtone?.stop()
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