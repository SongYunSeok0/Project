package com.myrhythm.alarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
    private val tag = "AlarmActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(tag, "onCreate 호출!")

        // 1. 화면 깨우기 & 잠금화면 위로 설정
        turnScreenOnAndKeyguard()

        // 2. Intent 데이터 수신 - ⭐ plan_id만 받기
        currentPlanId = intent.getLongExtra("PLAN_ID", 0L)

        Log.i(tag, "받은 데이터 - planId: $currentPlanId")

        if (currentPlanId == 0L) {
            Log.e(tag, "유효하지 않은 planId!")
            Toast.makeText(this, "알람 데이터 오류", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 보호자 여부 확인
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = type == "missed_alarm"

        Log.i(tag, "보호자 모드: $isGuardian")

        // 3. ⭐ 데이터 로드 - plan_id만 전달
        viewModel.loadData(currentPlanId)

        // 4. 소리 재생
        playAlarmSound()

        // 5. 이벤트 관찰
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is AlarmViewModel.AlarmEvent.Success -> {
                        Toast.makeText(
                            this@AlarmActivity,
                            "처리되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        stopAlarmAndFinish()
                    }
                    is AlarmViewModel.AlarmEvent.Error -> {
                        Toast.makeText(
                            this@AlarmActivity,
                            event.msg,
                            Toast.LENGTH_SHORT
                        ).show()
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
//                    onStop = {
//                        Log.i(tag, "보호자 화면 - 확인 버튼 클릭")
//                        stopAlarmAndFinish()
//                    },
//                      onDismiss = {
//                          Log.i(tag, "알람 끄기 버튼 클릭")
//                          stopAlarmAndFinish()
//                      }
//                )
//            } else {
            // 환자 화면
            PatientScreen(
                username = uiState.username,
                medicineLabel = uiState.medicineLabel,
                takenAtTime = uiState.takenAtTime,
                mealTime = uiState.mealTime,
                note = uiState.note,
                isOwnDevice = uiState.isOwnDevice,
                onStop = {
                    Log.i(tag, "환자 화면 - 복용 완료 버튼 클릭")
                    viewModel.markAsTaken(currentPlanId)
                },
                onSnooze = {
                    Log.i(tag, "환자 화면 - 미루기 버튼 클릭")
                    viewModel.snooze(currentPlanId)
                },
                onDismiss = {
                    Log.i(tag, "알람 끄기 버튼 클릭")
                    stopAlarmAndFinish()
                }
            )
        }
        //}
    }

    private fun turnScreenOnAndKeyguard() {
        Log.i(tag, "화면 깨우기 시작")

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

        Log.i(tag, "화면 깨우기 완료")
    }

    private fun playAlarmSound() {
        try {
            Log.i(tag, "알람 소리 재생 시작")
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
            Log.i(tag, "알람 소리 재생 중")
        } catch (e: Exception) {
            Log.e(tag, "알람 소리 재생 실패", e)
            e.printStackTrace()
        }
    }

    private fun stopAlarmAndFinish() {
        Log.i(tag, "알람 정지 및 종료")

        ringtone?.stop()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy 호출")
        ringtone?.stop()
    }
}