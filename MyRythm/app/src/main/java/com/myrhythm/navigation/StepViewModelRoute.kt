package com.myrhythm.navigation

import androidx.compose.runtime.*
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypage.viewmodel.MyPageViewModel
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.myrhythm.health.StepViewModel
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
            showGuardianDialog = p.prot_email.isNullOrBlank()

            // 아직 서버에서 로딩되지 않았으면 아무것도 하지 않음
            if (p == null) return@LaunchedEffect

            // 이미 한번 팝업 뜬 적 있으면 다시 뜨지 않음
            if (hasShownGuardianDialog) return@LaunchedEffect

            // prot_email 비어있으면 팝업 ON
            if (p.prot_email.isNullOrBlank()) {
                showGuardianDialog = true
            } else {
                showGuardianDialog = false
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
            Toast.makeText(context, "Health Connect 설치 필요", Toast.LENGTH_LONG).show()

            val url =
                "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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

        MainScreen(
            onOpenChatBot = onOpenChatBot,
            onOpenScheduler = onOpenScheduler,
            onOpenHeart = onOpenHeart,
            onOpenMap = onOpenMap,
            onOpenNews = onOpenNews,
            todaySteps = todaySteps,
            remainText = remainText
        )
    }
}