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
import androidx.compose.runtime.saveable.rememberSaveable
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

    // 이미 팝업을 띄운 적 있는지 확인
    var hasShownGuardianDialog by rememberSaveable { mutableStateOf(false) }

    // 실제로 화면에 보여줄 팝업 상태
    var showGuardianDialog by rememberSaveable { mutableStateOf(false) }

    // profile이 서버에서 로딩된 것을 의미 (null → 값)
    val isProfileReady = profile != null

    // 팝업 표시 로직 (안정 버전)
    LaunchedEffect(profile) {
        val p = profile
        if (p != null) {
            // p.prot_email.isNullOrBlank() 일 때 팝업 필요 여부 판단
            // showGuardianDialog = p.prot_email.isNullOrBlank() // -> 이 줄은 아래 로직과 중복되므로 제거하거나 아래 로직으로 통합

            // 아직 서버에서 로딩되지 않았으면 아무것도 하지 않음 (위의 null check로 이미 걸러짐)
            // if (p == null) return@LaunchedEffect

            // 이미 한번 팝업 뜬 적 있으면 다시 뜨지 않음
            if (hasShownGuardianDialog) return@LaunchedEffect

            // prot_email 비어있으면 팝업 ON
            if (p.prot_email.isNullOrBlank()) {
                showGuardianDialog = true
            } else {
                showGuardianDialog = false
            }
        }
    }

    // 팝업 UI (profile이 null이 아님 + 팝업 ON 인 경우만)
    if (isProfileReady && showGuardianDialog) {
        AlertDialog(
            onDismissRequest = { /* 뒤로가기 막기 */ },

            title = {
                Text("추가 정보가 필요해요 😊")
            },

            text = {
                Text("원활한 사용을 위해 보호자 이메일을 입력해 주세요!")
            },

            confirmButton = {
                Text(
                    text = "정보 입력하기",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            hasShownGuardianDialog = true     // 이제 다시 안 뜸
                            showGuardianDialog = false
                            onOpenEditScreen()
                        }
                )
            }
        )
    }

    // Health Connect 권한 체크 로직
    LaunchedEffect(Unit) {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.e("HC", "SDK STATUS = $status")
    }

    val installed =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    if (!installed) {
        // 설치되어 있지 않으면 Toast 띄우고 마켓으로 이동 (단, UI 컴포저블 내부에서 startActivity는 지양하고, LaunchedEffect나 onClick 등에서 처리하는 것이 좋음. 여기서는 일단 기존 로직 유지)
        // 주의: Recomposition이 일어날 때마다 실행될 수 있음.
        // Toast.makeText(context, "Health Connect 설치 필요", Toast.LENGTH_LONG).show()
        // ...
        // 아래 로직은 SideEffect로 감싸는 것이 안전함. 일단 기존 코드 흐름 유지하되, installed가 false면 조기 리턴
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

    LaunchedEffect(Unit) {
        stepViewModel.checkPermission()
    }

    LaunchedEffect(granted) {
        if (!granted) {
            permissionLauncher.launch(stepViewModel.requestPermissions())
        } else {
            stepViewModel.startAutoUpdateOnce(intervalMillis = 5_000L)
        }
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
                    Toast.makeText(context, "시간이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                },
                hour,
                minute,
                false // true=24시간제, false=오전/오후 선택
            ).apply {
                // 라벨(병명)이 있으면 "감기약 시간 변경", 없으면 약 이름으로 표시
                // Plan 데이터 클래스에 regihistoryLabel 필드가 추가되어 있어야 함
//                val titleText = nextPlan!!.regihistoryLabel ?: nextPlan!!.medName ?: "약"
//                setTitle("$titleText 시간 변경")
            }.show()
        } else {
            Toast.makeText(context, "예정된 복용 일정이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,
        onOpenAlram = { openTimePicker() }, // ✅ 알람 클릭 연결
        todaySteps = todaySteps,
        remainText = remainText
    )
}