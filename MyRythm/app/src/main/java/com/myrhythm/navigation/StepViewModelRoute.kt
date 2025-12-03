package com.myrhythm.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController

import com.domain.sharedvm.MainVMContract
import com.domain.sharedvm.HeartRateVMContract
import com.domain.sharedvm.StepVMContract

import com.mypage.viewmodel.MyPageViewModel
import com.shared.ui.MainScreen
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun StepViewModelRoute(
    mainViewModel: MainVMContract,
    heartViewModel: HeartRateVMContract,
    stepViewModel: StepVMContract,
    myPageViewModel: MyPageViewModel,
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenEditScreen: () -> Unit = {}
) {
    val context = LocalContext.current

    val nextPlan by mainViewModel.nextPlan.collectAsStateWithLifecycle()
    val nextLabel by mainViewModel.nextLabel.collectAsStateWithLifecycle()
    val remainText by mainViewModel.remainText.collectAsStateWithLifecycle()

    val todaySteps by stepViewModel.todaySteps.collectAsStateWithLifecycle()
    val previewExtend by mainViewModel.previewExtendMinutes.collectAsStateWithLifecycle()

    val profile by myPageViewModel.profile.collectAsStateWithLifecycle()

    val latestBpm by heartViewModel.latestHeartRate.collectAsStateWithLifecycle()

    var hasShownGuardianDialog by remember { mutableStateOf(false) }
    var showGuardianDialog by remember { mutableStateOf(false) }
    var showExtendDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // 보호자 이메일 팝업
    LaunchedEffect(profile) {
        val p = profile ?: return@LaunchedEffect
        if (!hasShownGuardianDialog && p.prot_email.isNullOrBlank()) {
            showGuardianDialog = true
        }
    }

    if (showGuardianDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("추가 정보가 필요해요 😊") },
            text = { Text("원활한 사용을 위해 보호자 이메일을 입력해 주세요!") },
            confirmButton = {
                Text(
                    "정보 입력하기",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            hasShownGuardianDialog = true
                            showGuardianDialog = false
                            onOpenEditScreen()
                        }
                )
            }
        )
    }

    // HealthConnect 권한 체크
    val installed = HealthConnectClient.getSdkStatus(context) ==
            HealthConnectClient.SDK_AVAILABLE

    // HealthConnect가 설치된 경우에만 권한 관련 로직 실행
    if (installed) {
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

        LaunchedEffect(Unit) {
            stepViewModel.checkPermission()
        }

        LaunchedEffect(granted) {
            if (!granted) {
                permissionLauncher.launch(stepViewModel.requestPermissions())
            } else {
                stepViewModel.startAutoUpdateOnce(5_000)
            }
        }
    }

    // 심박수 동기화 (HealthConnect 없어도 실행)
    LaunchedEffect(Unit) {
        heartViewModel.syncHeartHistory()
    }

    val onAlarmCardClick = {
        if (nextPlan != null) {
            mainViewModel.clearPreview()
            showExtendDialog = true
        } else {
            Toast.makeText(context, "예정된 복용 일정이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showExtendDialog && nextPlan != null) {

        val previewRemain = run {
            val base = nextPlan?.takenAt ?: 0L
            val previewTime = base + previewExtend * 60_000L

            val diff = previewTime - System.currentTimeMillis()
            val mins = diff / 1000 / 60
            val h = mins / 60
            val m = mins % 60

            String.format(Locale.getDefault(), "%02d:%02d", h, m)
        }

        AlertDialog(
            onDismissRequest = { showExtendDialog = false },

            title = {
                Text(
                    text = nextLabel ?: "다음 복용",
                    style = MaterialTheme.typography.titleLarge
                )
            },

            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = previewRemain,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExtendChip(text = "+5분", modifier = Modifier.weight(1f)) {
                            mainViewModel.previewExtend(5)
                        }
                        ExtendChip(text = "+10분", modifier = Modifier.weight(1f)) {
                            mainViewModel.previewExtend(10)
                        }
                        ExtendChip(text = "+15분", modifier = Modifier.weight(1f)) {
                            mainViewModel.previewExtend(15)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BottomActionButton("복용완료", Modifier.weight(1f)) {
                            mainViewModel.finishPlan()
                            mainViewModel.clearPreview()
                            showExtendDialog = false
                        }

                        BottomActionButton("취소", Modifier.weight(1f)) {
                            mainViewModel.clearPreview()
                            showExtendDialog = false
                        }

                        BottomActionButton("확인", Modifier.weight(1f)) {
                            scope.launch {
                                val ok = mainViewModel.extendPlanMinutesSuspend(previewExtend)
                                if (ok) {
                                    mainViewModel.clearPreview()
                                    showExtendDialog = false
                                }
                            }
                        }
                    }
                }
            },

            confirmButton = {}
        )
    }

    // MainScreen은 HealthConnect 유무와 관계없이 항상 표시
    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,
        onOpenAlram = onAlarmCardClick,
        todaySteps = if (installed) todaySteps else 0,  // HealthConnect 없으면 0
        remainText = remainText,
        nextLabel = nextLabel
    )
}

/* 연장 버튼 */
@Composable
fun ExtendChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/* 액션 버튼 */
@Composable
fun BottomActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}