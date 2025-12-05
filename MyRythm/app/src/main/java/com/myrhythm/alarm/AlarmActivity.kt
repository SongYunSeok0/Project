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
import com.myrhythm.alarm.ui.GuardianScreen
import com.myrhythm.alarm.ui.PatientScreen
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

        // 0. 디버깅용: 받은 모든 Intent Extra 로그 출력
        intent.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                Log.d(tag, "Intent Key: $key, Value: ${bundle.get(key)}")
            }
        }

        // 1. 화면 깨우기 & 잠금화면 위로 설정
        turnScreenOnAndKeyguard()

        // 2. Plan ID 파싱 (PLAN_ID / plan_id 모두 대응)
        currentPlanId = getSafePlanId()

        // 3. 보호자 여부 판정 (FCM data / Intent "type" 기준)
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = (type == "missed_alarm")

        Log.i(tag, "보호자 모드 판정: $isGuardian (type=$type)")
        Log.i(tag, "받은 Plan ID: $currentPlanId")

        // 4. 유효성 검사
        //    - 환자 모드: planId 필수
        //    - 보호자 모드: planId 없어도(0L) 화면은 띄울 수 있음
        if (currentPlanId == 0L && !isGuardian) {
            Log.e(tag, "유효하지 않은 planId! (0L) - 환자 모드이므로 종료")
            Toast.makeText(this, "알람 데이터 오류", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 5. 데이터 로드 (Plan ID가 있을 때만)
        if (currentPlanId != 0L) {
            viewModel.loadData(currentPlanId)
        }

        // 6. 알람 소리 재생
        playAlarmSound()

        // 7. ViewModel 이벤트 관찰
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

        // 8. UI 표시 (Compose)
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            if (isGuardian) {
                // 🔵 보호자 화면
                val intentUserName = intent.getStringExtra("user_name")
                val intentMedName = intent.getStringExtra("med_name")

                // Intent 데이터 우선 사용 → 없으면 ViewModel 값 → 그래도 없으면 기본값
                val displayUsername =
                    if (!intentUserName.isNullOrBlank()) intentUserName else uiState.username
                val displayMedName =
                    if (!intentMedName.isNullOrBlank()) intentMedName else uiState.medicineLabel

                // 전화번호는 DB 기반 (없으면 빈 문자열)
                val displayPhone = uiState.phoneNumber

                Log.d(
                    tag,
                    "UI: 보호자 화면 표시 - 환자: $displayUsername / 약: $displayMedName / phone: $displayPhone"
                )

                GuardianScreen(
                    username = displayUsername,
                    medicineLabel = displayMedName,
                    patientPhone = displayPhone,
                    onClose = {
                        Log.i(tag, "보호자 화면 - 닫기 버튼 클릭")
                        stopAlarmAndFinish()
                    }
                )
            } else {
                // 🟢 환자 화면
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
                        Log.i(tag, "환자 화면 - 알람 끄기 버튼 클릭")
                        stopAlarmAndFinish()
                    }
                )
            }
        }
    }

    /**
     * Intent 에서 PLAN_ID / plan_id 를 안전하게 읽는 헬퍼
     */
    private fun getSafePlanId(): Long {
        var id = intent.getLongExtra("PLAN_ID", 0L)
        if (id != 0L) return id

        id = intent.getLongExtra("plan_id", 0L)
        if (id != 0L) return id

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
