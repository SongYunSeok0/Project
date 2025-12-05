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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.myrhythm.alarm.ui.PatientScreen
// import com.myrhythm.alarm.ui.GuardianScreen // 보호자 스크린이 있다면 import
import com.myrhythm.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // collect 대신 collectLatest 사용 권장
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()
    private var ringtone: Ringtone? = null
    private var currentPlanId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 화면 깨우기
        turnScreenOnAndKeyguard()

        // 2. Intent 데이터 수신 (FCM Service에서 보낸 키값 "plan_id"와 일치해야 함)
        val planIdStr = intent.getStringExtra("plan_id") ?: "-1"
        currentPlanId = planIdStr.toLongOrNull() ?: -1L

        // UI에 표시할 추가 데이터 (필요 시)
        val title = intent.getStringExtra("title") ?: "약 드실 시간이에요!"
        val body = intent.getStringExtra("body") ?: "복약 시간입니다"

        // 보호자 여부 확인
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = type == "missed_alarm" || intent.getStringExtra("is_guardian") == "true"

        // 3. 데이터 로드 (ViewModel에 해당 함수가 있다면 호출)
        // viewModel.loadData(userId, currentPlanId, regiId)
        // -> 만약 loadData가 없다면 FCM에서 받은 title, body를 UIState에 넣는 로직이 필요하거나
        //    PatientScreen에 직접 title, body를 넘겨야 합니다.
        //    여기서는 일단 버튼 기능 연결에 집중합니다.

        // 4. 소리 재생
        playAlarmSound()

        // 5. 이벤트 관찰 (성공 시 종료 처리)
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is AlarmViewModel.AlarmEvent.Success -> {
                        Toast.makeText(this@AlarmActivity, "처리되었습니다.", Toast.LENGTH_SHORT).show()
                        stopAlarmAndFinish()
                    }
                    is AlarmViewModel.AlarmEvent.Error -> {
                        Toast.makeText(this@AlarmActivity, event.msg, Toast.LENGTH_SHORT).show()
                        stopAlarmAndFinish() // 에러가 나도 일단 알람은 끄는 게 좋음
                    }
                }
            }
        }

        // 6. UI 표시
        setContent {
            // ViewModel에 uiState가 구현되어 있다고 가정
            val uiState by viewModel.uiState.collectAsState()

            // 만약 ViewModel에서 데이터를 로드하지 못했다면 Intent에서 받은 기본값 사용
            val displayUsername = if(uiState.username.isNotEmpty()) uiState.username else "환자"
            val displayLabel = if(uiState.medicineLabel.isNotEmpty()) uiState.medicineLabel else body

            if (isGuardian) {
                // [보호자 화면] - 코드가 있다면 주석 해제
                /*
                GuardianScreen(
                    username = displayUsername,
                    medicineLabel = displayLabel,
                    takenAtTime = uiState.takenAtTime,
                    mealTime = uiState.mealTime,
                    note = uiState.note,
                    onStop = { stopAlarmAndFinish() }
                )
                */
            } else {
                // [환자 화면]
                PatientScreen(
                    username = displayUsername,
                    medicineLabel = displayLabel,
                    takenAtTime = uiState.takenAtTime,
                    mealTime = uiState.mealTime,
                    note = uiState.note,

                    // [버튼 1] 복약 완료 (UI의 onStop)
                    onStop = {
                        if (currentPlanId != -1L) {
                            viewModel.markAsTaken(currentPlanId)
                        } else {
                            stopAlarmAndFinish()
                        }
                    },

                    // [버튼 2] 미루기 (UI의 onSnooze)
                    onSnooze = {
                        if (currentPlanId != -1L) {
                            viewModel.snooze(currentPlanId)
                        } else {
                            stopAlarmAndFinish()
                        }
                    }
                )
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