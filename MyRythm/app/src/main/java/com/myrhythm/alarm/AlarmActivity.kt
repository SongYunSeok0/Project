package com.myrhythm.alarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.myrhythm.alarm.ui.PatientScreen
import com.myrhythm.alarm.ui.GuardianScreen
import com.myrhythm.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()
    private var ringtone: Ringtone? = null
    private var currentPlanId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 화면 깨우기 & 잠금화면 위로 설정
        turnScreenOnAndKeyguard()

        // 2. Intent 데이터 수신
        currentPlanId = intent.getLongExtra("PLAN_ID", 0L)
        val regiId = intent.getLongExtra("REGI_ID", 0L)
        val userId = intent.getStringExtra("USER_ID") ?: ""

        // 보호자 여부 확인
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = type == "missed_alarm" || intent.getStringExtra("is_guardian") == "true"

        // 3. 데이터 로드
        viewModel.loadData(userId, currentPlanId, regiId)

        // 4. 소리 재생
        playAlarmSound()

        // 5. 이벤트 관찰
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is AlarmViewModel.AlarmEvent.Success -> {
                        Toast.makeText(this@AlarmActivity, "처리되었습니다.", Toast.LENGTH_SHORT).show()
                        stopAlarmAndFinish()
                    }
                    is AlarmViewModel.AlarmEvent.Error -> {
                        Toast.makeText(this@AlarmActivity, event.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 6. UI 표시 (Compose)
        setContent {
            val uiState by viewModel.uiState.collectAsState()

//            if (isGuardian) {
//                // 보호자 화면
//                GuardianScreen(
//                    username = uiState.username,
//                    medicineLabel = uiState.medicineLabel,
//                    takenAtTime = uiState.takenAtTime,
//                    mealTime = uiState.mealTime,
//                    note = uiState.note,
//                    onStop = { stopAlarmAndFinish() }
//                )
//            } else {
                // 환자 화면
                PatientScreen(
                    username = uiState.username,
                    medicineLabel = uiState.medicineLabel,
                    takenAtTime = uiState.takenAtTime,
                    mealTime = uiState.mealTime,
                    note = uiState.note,
                    onStop = {
                        viewModel.markAsTaken(currentPlanId)
                    },
                    onSnooze = {
                        viewModel.snooze(currentPlanId)
                    }
                )
            }
        }
//    }

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