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

        // 0. 디버깅용: 받은 모든 데이터 로그 출력
        intent.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                Log.d(tag, "Intent Key: $key, Value: ${bundle.get(key)}")
            }
        }

        // 1. 화면 깨우기 & 잠금화면 위로 설정
        turnScreenOnAndKeyguard()

        // 2. Intent 데이터 수신 - ⭐ 안전하게 받기 (String/Long, 대소문자 모두 체크)
        currentPlanId = getSafePlanId()

        Log.i(tag, "받은 데이터 - planId: $currentPlanId")

        if (currentPlanId == 0L) {
            Log.e(tag, "유효하지 않은 planId! (0L)")
            Toast.makeText(this, "알람 데이터 오류", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ⭐ 보호자 여부 확인 로직 강화
        // 서버에서 "type": "missed_alarm"과 "is_guardian": "true"를 보냄
        val type = intent.getStringExtra("type")
        val isGuardianParam = intent.getStringExtra("is_guardian")

        // type이 missed_alarm 이거나, is_guardian이 "true"이면 보호자 모드
        val isGuardian = (type == "missed_alarm") || (isGuardianParam == "true")

        Log.i(tag, "보호자 모드 판정: $isGuardian (type=$type, is_guardian=$isGuardianParam)")

        // 3. 데이터 로드 - plan_id만 전달
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

            if (isGuardian) {
                // ⭐ 보호자 화면
                Log.d(tag, "UI: 보호자 화면 표시")
                GuardianScreen(
                    username = uiState.username,
                    medicineLabel = uiState.medicineLabel,
                    patientPhone= uiState.phoneNumber,
                    onClose = {
                        stopAlarmAndFinish()
                    }
                )
//                username: String,
//                medicineLabel: String,
//                patientPhone: String,
//                onClose: () -> Unit
            } else {
                // 환자 화면
                Log.d(tag, "UI: 환자 화면 표시")
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
        }
    }

    // FCM 데이터(String)와 내부 Intent(Long) 호환을 위한 안전한 ID 추출 함수
    private fun getSafePlanId(): Long {
        // 1. Long 타입으로 시도 ("PLAN_ID")
        var id = intent.getLongExtra("PLAN_ID", 0L)
        if (id != 0L) return id

        // 2. Long 타입으로 시도 ("plan_id") - 소문자 키
        id = intent.getLongExtra("plan_id", 0L)
        if (id != 0L) return id

        // 3. String 타입으로 받아서 변환 시도 (FCM data payload는 주로 String임)
        val idStr = intent.getStringExtra("plan_id") ?: intent.getStringExtra("PLAN_ID")
        return idStr?.toLongOrNull() ?: 0L
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