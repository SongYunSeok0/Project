package com.myrhythm.navigation

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.myrhythm.health.StepViewModel
import com.mypage.viewmodel.MyPageViewModel
import com.myrhythm.viewmodel.MainViewModel
import com.shared.ui.MainScreen
import java.util.Calendar

@Composable
fun StepViewModelRoute(
    myPageViewModel: MyPageViewModel,
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenEditScreen: () -> Unit = {},
) {
    val context = LocalContext.current
    val stepViewModel: StepViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()

    // ViewModel 데이터 구독
    val nextTime by mainViewModel.nextTime.collectAsStateWithLifecycle()
    val remainText by mainViewModel.remainText.collectAsStateWithLifecycle()
    val nextPlan by mainViewModel.nextPlan.collectAsStateWithLifecycle() // ✅ 다음 약 정보 구독

    val profile by myPageViewModel.profile.collectAsStateWithLifecycle()
    var showGuardianDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        val p = profile
        if (p != null) {
            showGuardianDialog = p.prot_email.isNullOrBlank()
        }
    }

    // 보호자 이메일 입력 유도 다이얼로그
    if (showGuardianDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("추가 정보가 필요해요 😊") },
            text = { Text("원활한 사용을 위해 보호자 이메일을 입력해 주세요!") },
            confirmButton = {
                Text(
                    text = "정보 입력하기",
                    modifier = Modifier.padding(8.dp).clickable {
                        showGuardianDialog = false
                        onOpenEditScreen()
                    }
                )
            }
        )
    }

    // Health Connect 권한 체크 로직
    val installed = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    if (!installed) {
        Toast.makeText(context, "Health Connect 설치 필요", Toast.LENGTH_LONG).show()
        val url = "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"
        val installIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(installIntent)
        return
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(stepViewModel.requestPermissions())) {
            stepViewModel.checkPermission()
        } else {
            Toast.makeText(context, "걸음수 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val granted by stepViewModel.permissionGranted.collectAsStateWithLifecycle()
    val todaySteps by stepViewModel.todaySteps.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { stepViewModel.checkPermission() }
    LaunchedEffect(granted) {
        if (!granted) permissionLauncher.launch(stepViewModel.requestPermissions())
        else stepViewModel.startAutoUpdateOnce(5_000L)
    }

    // ============================================================
    // ✅ [핵심] 시간 변경 다이얼로그 띄우기 로직
    // ============================================================
    val calendar = Calendar.getInstance()

    val openTimePicker = {
        Log.d("MyRhythm", "알람 카드 클릭됨. nextPlan: ${nextPlan?.medName}") // 디버깅 로그

        if (nextPlan != null) {
            // 현재 설정된 시간을 가져와서 캘린더에 세팅
            val currentTakenAt = nextPlan!!.takenAt ?: System.currentTimeMillis()
            calendar.timeInMillis = currentTakenAt
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // 안드로이드 기본 타임피커 표시
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    Log.d("MyRhythm", "시간 변경 선택: $selectedHour:$selectedMinute")
                    // [확인] 버튼 클릭 시 -> ViewModel에 업데이트 요청
                    mainViewModel.updatePlanTime(nextPlan!!.id!!, selectedHour, selectedMinute)
                    Toast.makeText(context, "${nextPlan!!.medName} 시간이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                },
                hour,
                minute,
                false // true=24시간제, false=오전/오후 선택
            ).apply {
                setTitle("${nextPlan!!.medName} 시간 변경")
            }.show()
        } else {
            Toast.makeText(context, "예정된 복용 일정이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 메인 화면 호출
    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,

        // ⭐ [중요] 여기가 빠져 있어서 팝업이 안 떴던 것입니다!
        onOpenAlram = { openTimePicker() },

        todaySteps = todaySteps,
        nextTime = nextTime,
        remainText = remainText,
    )
}