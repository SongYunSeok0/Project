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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myrhythm.alarm.ui.PatientScreen
import com.myrhythm.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 화면 깨우기 & 잠금화면 위로 설정
        turnScreenOnAndKeyguard()

        // 2. Intent 데이터 수신 (AppFcmService에서 보낸 키값과 일치해야 함)
        val planIdStr = intent.getStringExtra("plan_id") ?: "-1"
        val planId = planIdStr.toLongOrNull() ?: -1L

        // 상세 데이터 수신
        val username = intent.getStringExtra("user_name") ?: ""
        val medicineLabel = intent.getStringExtra("med_name") ?: ""
        val takenAtTime = intent.getStringExtra("taken_at") ?: ""
        val mealTime = intent.getStringExtra("meal_time") ?: ""
        val note = intent.getStringExtra("note") ?: ""

        // 기본 데이터 (Fallback용)
        val title = intent.getStringExtra("title") ?: "약 드실 시간이에요!"
        val body = intent.getStringExtra("body") ?: "복약 시간입니다"

        // 보호자 여부 확인
        val type = intent.getStringExtra("type") ?: "ALARM"
        val isGuardian = type == "missed_alarm" || intent.getStringExtra("is_guardian") == "true"

        // 3. 소리 재생
        playAlarmSound()

        // 4. UI 표시 (Compose)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    // 성공/실패 이벤트 관찰
                    LaunchedEffect(key1 = true) {
                        viewModel.eventFlow.collectLatest { event ->
                            when(event) {
                                is AlarmViewModel.AlarmEvent.Success -> {
                                    Toast.makeText(this@AlarmActivity, "처리되었습니다.", Toast.LENGTH_SHORT).show()
                                    stopAlarmAndFinish()
                                }
                                is AlarmViewModel.AlarmEvent.Error -> {
                                    Toast.makeText(this@AlarmActivity, event.msg, Toast.LENGTH_SHORT).show()
                                    stopAlarmAndFinish()
                                }
                            }
                        }
                    }

                    if (isGuardian) {
                        // [보호자용] 간단한 알림 화면
                        GuardianSimpleScreen(
                            title = title,
                            body = body,
                            onConfirm = { stopAlarmAndFinish() }
                        )
                    } else {
                        // [환자용] 상세 정보 화면 (PatientScreen 사용)
                        // 데이터가 비어있으면 기본값(title, body)을 사용하여 표시
                        PatientScreen(
                            username = if (username.isNotBlank()) username else "환자",
                            medicineLabel = if (medicineLabel.isNotBlank()) medicineLabel else title,
                            takenAtTime = takenAtTime,
                            mealTime = mealTime,
                            note = if (note.isNotBlank()) note else body,
                            onStop = {
                                if (planId != -1L) viewModel.markAsTaken(planId)
                                else stopAlarmAndFinish()
                            },
                            onSnooze = {
                                if (planId != -1L) viewModel.snooze(planId)
                                else stopAlarmAndFinish()
                            }
                        )
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

/**
 * 보호자용 간단 화면 (파일 하단에 포함)
 */
@Composable
fun GuardianSimpleScreen(
    title: String,
    body: String,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🚨", fontSize = 60.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = body, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("확인 (알람 끄기)")
        }
    }
}