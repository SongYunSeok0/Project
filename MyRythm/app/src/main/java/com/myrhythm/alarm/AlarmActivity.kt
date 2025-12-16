package com.myrhythm.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
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
        Log.e(tag, "🔥 AlarmActivity onCreate 호출")

        // 1. 화면 깨우기 및 잠금 해제 설정 (가장 먼저 실행)
        turnScreenOnAndKeyguard()

        // 2. 데이터 처리 및 UI 설정
        processIntent(intent)
    }

    /**
     * 이미 알람 화면이 떠 있는데 새로운 알람(FCM)이 또 왔을 때 호출됨
     * (AndroidManifest에서 launchMode="singleTask" 설정 필수)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e(tag, "🔄 onNewIntent 호출 - 새로운 알람 데이터 갱신")

        // 새로운 인텐트로 교체
        setIntent(intent)

        // 기존 링톤 끄고 다시 시작 (선택 사항)
        ringtone?.stop()

        // 화면 다시 깨우기
        turnScreenOnAndKeyguard()

        // 데이터 재처리
        intent?.let { processIntent(it) }
    }

    private fun processIntent(intent: Intent) {
        // 0. 디버깅 로그
        logIntentExtras(intent)

        // 1. Plan ID 파싱
        currentPlanId = getSafePlanId(intent)

        // 2. 보호자 여부 확인
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = (type == "missed_alarm")

        Log.e(tag, "🔍 모드: ${if (isGuardian) "보호자" else "환자"} (type=$type, planId=$currentPlanId)")

        // 3. 유효성 검사 (환자 모드인데 ID 없으면 종료)
        if (currentPlanId == 0L && !isGuardian) {
            Log.e(tag, "❌ 환자 모드인데 Plan ID 없음. 종료.")
            finish()
            return
        }

        // 4. ViewModel 데이터 로드
        if (currentPlanId != 0L) {
            viewModel.loadData(currentPlanId)
        }

        // 5. 소리 재생
        playAlarmSound()

        // 6. 이벤트 관찰 (성공/실패 토스트)
        observeViewModelEvents()

        // 7. UI 그리기
        setupComposeUI(isGuardian, intent)
    }

    private fun setupComposeUI(isGuardian: Boolean, intent: Intent) {
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            if (isGuardian) {
                // 🔵 보호자 화면 데이터 준비
                // Intent 데이터를 최우선으로, 없으면 ViewModel(State) 사용
                val displayUsername = intent.getStringExtra("user_name")
                    ?: intent.getStringExtra("username")
                    ?: uiState.username

                val displayMedName = intent.getStringExtra("med_name")
                    ?: intent.getStringExtra("body") // FCM body를 약 이름으로 쓸 경우
                    ?: uiState.medicineLabel

                val displayPhone = intent.getStringExtra("patient_phone")
                    ?: uiState.phoneNumber

                GuardianScreen(
                    username = displayUsername,
                    medicineLabel = displayMedName,
                    patientPhone = displayPhone,
                    onClose = {
                        stopAlarmAndFinish()
                    }
                )
            } else {
                // 🟢 환자 화면
                PatientScreen(
                    username = uiState.username,
                    medicineLabel = uiState.medicineLabel,
                    takenAtTime = uiState.takenAtTime,
                    mealTime = uiState.mealTime,
                    note = uiState.note,
                    isOwnDevice = uiState.isOwnDevice,
                    onStop = {
                        viewModel.markAsTaken(currentPlanId)
                    },
                    onSnooze = {
                        viewModel.snooze(currentPlanId)
                        stopAlarmAndFinish() // 미루기 누르면 일단 알람 화면은 끔
                    },
                    onDismiss = {
                        stopAlarmAndFinish()
                    }
                )
            }
        }
    }

    private fun observeViewModelEvents() {
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
    }

    private fun turnScreenOnAndKeyguard() {
        Log.i(tag, "🔓 화면 깨우기 및 잠금해제 요청")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            // ⭐ 핵심: 잠금화면(키가드) 해제 요청 (패턴 입력 없이 바로 보이게)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or // 구버전 잠금해제
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun playAlarmSound() {
        try {
            if (ringtone?.isPlaying == true) return // 이미 재생 중이면 패스

            Log.i(tag, "🔊 알람 소리 재생 시작")
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)

            // ⭐ 오디오 속성 설정 (알람 볼륨 채널 사용)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }

            ringtone?.play()
        } catch (e: Exception) {
            Log.e(tag, "❌ 알람 소리 재생 실패", e)
        }
    }

    private fun stopAlarmAndFinish() {
        Log.i(tag, "🛑 알람 종료")
        try {
            ringtone?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask() // 앱 목록(Recents)에서도 제거 깔끔하게
        } else {
            finish()
        }
    }

    private fun getSafePlanId(intent: Intent): Long {
        // FCM data는 모두 String으로 옴. 따라서 String -> Long 변환이 가장 안전함.
        val idStr = intent.getStringExtra("plan_id") ?: intent.getStringExtra("PLAN_ID")
        val idFromString = idStr?.toLongOrNull()
        if (idFromString != null && idFromString != 0L) return idFromString

        // 혹시 모르니 LongExtra도 체크
        val idLong = intent.getLongExtra("plan_id", 0L)
        if (idLong != 0L) return idLong

        return intent.getLongExtra("PLAN_ID", 0L)
    }

    private fun logIntentExtras(intent: Intent) {
        intent.extras?.let { bundle ->
            Log.e(tag, "📦 Intent Data:")
            for (key in bundle.keySet()) {
                Log.e(tag, " - $key : ${bundle.get(key)}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "💀 onDestroy")
        ringtone?.stop()
    }
}