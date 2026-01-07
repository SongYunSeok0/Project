package com.myrhythm.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.domain.sharedvm.HeartRateVMContract
import com.domain.sharedvm.MainVMContract
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
    onOpenHealthInsight: () -> Unit,
    onOpenEditScreen: () -> Unit = {}
) {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    }

    val nextPlan by mainViewModel.nextPlan.collectAsStateWithLifecycle()
    val nextLabel by mainViewModel.nextLabel.collectAsStateWithLifecycle()
    val remainText by mainViewModel.remainText.collectAsStateWithLifecycle()

    val todaySteps by stepViewModel.todaySteps.collectAsStateWithLifecycle()
    val previewExtend by mainViewModel.previewExtendMinutes.collectAsStateWithLifecycle()

    val profile by myPageViewModel.profile.collectAsStateWithLifecycle()
    val hasGuardian = profile?.prot_email?.isNotBlank() == true

    // ✅ 프로필 로드(기존 유지)
    LaunchedEffect(Unit) {
        android.util.Log.e("PROFILE_LOAD", "✅ calling loadProfile()")
        myPageViewModel.loadProfile()
    }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showExtendDialog by remember { mutableStateOf(false) }

    // ✅ 권한 플로우 상태
    var permissionDialogShown by remember { mutableStateOf(false) } // 런처 중복 호출 방지

    val scope = rememberCoroutineScope()

    // -----------------------------
    // 1) Health Connect 권한 먼저
    // -----------------------------
    val sdkStatus = remember {
        HealthConnectClient.getSdkStatus(context)
    }
    android.util.Log.e("HC_STATUS", "sdkStatus=$sdkStatus")

    val installed = sdkStatus == HealthConnectClient.SDK_AVAILABLE

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedSet ->
        // 권한 결과 수신 로그
        android.util.Log.e(
            "HC_PERM",
            "RESULT grantedSet=$grantedSet required=${stepViewModel.requestPermissions()} " +
                    "containsAll=${grantedSet.containsAll(stepViewModel.requestPermissions())}"
        )

        if (grantedSet.containsAll(stepViewModel.requestPermissions())) {
            stepViewModel.checkPermission()
            stepViewModel.startAutoUpdateOnce(5_000)
        } else {
            Toast.makeText(context, "걸음수 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val granted by stepViewModel.permissionGranted.collectAsStateWithLifecycle()

    // 최초 권한 상태 확인
    LaunchedEffect(Unit) {
        stepViewModel.checkPermission()
    }

    // ✅ 권한 우선 흐름
    LaunchedEffect(granted, installed) {
        if (!installed) {
            android.util.Log.e("HC_PERM", "Health Connect not available on this device.")
            return@LaunchedEffect
        }

        if (granted) {
            android.util.Log.e("HC_PERM", "Already granted -> start updates")
            stepViewModel.startAutoUpdateOnce(5_000)
            return@LaunchedEffect
        }

        // granted=false 인데 아직 요청 안 했으면 요청
        if (!permissionDialogShown) {
            val perms = stepViewModel.requestPermissions()
            android.util.Log.e("HC_PERM", "REQUEST granted=$granted perms=$perms size=${perms.size}")

            if (perms.isEmpty()) {
                android.util.Log.e("HC_PERM", "❌ requestPermissions() is EMPTY. 권한 팝업이 뜰 수 없음")
                // 이 경우엔 다이얼로그든 권한이든 흐름이 꼬이니, 여기서 그냥 리턴
                return@LaunchedEffect
            }

            permissionDialogShown = true
            permissionLauncher.launch(perms)
        }
    }

    // -----------------------------
    // 2) 프로필 팝업은 "권한 허용 후"에만
    //    (installed && !granted) 상태에서는 절대 띄우지 않음
    // -----------------------------
    data class ProfileCheckResult(
        val isSocialLogin: Boolean,
        val needsBasicInfo: Boolean,
        val needsGuardian: Boolean
    )

    val profileCheck = remember(profile) {
        profile?.let { p ->
            android.util.Log.e("ProfileCheck", "========== Profile 체크 ==========")
            android.util.Log.e("ProfileCheck", "username = '${p.username}'")
            android.util.Log.e("ProfileCheck", "phone = '${p.phone}'")
            android.util.Log.e("ProfileCheck", "prot_email = '${p.prot_email}'")
            android.util.Log.e("ProfileCheck", "email = '${p.email}'")

            val missingBasicInfo = p.username.isNullOrBlank() || p.phone.isNullOrBlank()
            val missingGuardian = p.prot_email.isNullOrBlank()

            android.util.Log.e("ProfileCheck", "missingBasicInfo = $missingBasicInfo")
            android.util.Log.e("ProfileCheck", "missingGuardian = $missingGuardian")

            ProfileCheckResult(
                isSocialLogin = missingBasicInfo,
                needsBasicInfo = missingBasicInfo,
                needsGuardian = missingGuardian
            )
        }
    }

    // ✅ 권한이 필요한 기기(installed)에서 granted=false이면 프로필 팝업 금지
    LaunchedEffect(profile, profileCheck, granted, installed) {
        if (installed && !granted) {
            android.util.Log.e("ProfileDialog", "⏸️ 권한 미허용 상태 → 프로필 팝업 보류")
            showProfileDialog = false
            return@LaunchedEffect
        }

        val check = profileCheck ?: run {
            android.util.Log.e("ProfileDialog", "❌ profileCheck is null (profile 로드 중)")
            return@LaunchedEffect
        }

        val hasClosedDialog = prefs.getBoolean("closed_profile_dialog", false)

        showProfileDialog = when {
            check.isSocialLogin -> {
                android.util.Log.e("ProfileDialog", "✅ 소셜 로그인 → 팝업 표시")
                true
            }

            !hasClosedDialog && check.needsGuardian -> {
                android.util.Log.e("ProfileDialog", "✅ 일반 로그인 + 보호자 없음 → 팝업 표시")
                true
            }

            else -> {
                android.util.Log.e("ProfileDialog", "❌ 팝업 표시 조건 불충족")
                false
            }
        }
    }

    // ✅ 프로필 팝업 렌더링
    if (showProfileDialog && profile != null && profileCheck != null) {
        val check = profileCheck!!

        if (check.isSocialLogin) {
            SocialLoginProfileDialog(
                onDismiss = {},
                onConfirm = {
                    showProfileDialog = false
                    onOpenEditScreen()
                }
            )
        } else {
            NormalLoginGuardianDialog(
                onDismiss = {
                    showProfileDialog = false
                    prefs.edit().putBoolean("closed_profile_dialog", true).apply()
                },
                onConfirm = {
                    showProfileDialog = false
                    prefs.edit().putBoolean("closed_profile_dialog", true).apply()
                    onOpenEditScreen()
                }
            )
        }
    }

    // -----------------------------
    // 나머지 기존 로직 유지
    // -----------------------------
    LaunchedEffect(Unit) {
        heartViewModel.syncHeartHistory()
    }

    val onAlarmCardClick = {
        if (nextPlan != null) {
            if (!hasGuardian) {
                Toast.makeText(
                    context,
                    "알림 기능을 사용하려면 보호자 이메일을 등록해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                mainViewModel.clearPreview()
                showExtendDialog = true
            }
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
            onDismissRequest = {
                mainViewModel.clearPreview()
                showExtendDialog = false
            },
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

    Box(modifier = Modifier.fillMaxSize()) {

        MainScreen(
            onOpenChatBot = onOpenChatBot,
            onOpenScheduler = onOpenScheduler,
            onOpenHeart = onOpenHeart,
            onOpenMap = onOpenMap,
            onOpenNews = onOpenNews,
            onOpenHealthInsight = onOpenHealthInsight,
            onOpenAlram = onAlarmCardClick,
            todaySteps = if (installed) todaySteps else 0,
            remainText = remainText,
            nextLabel = nextLabel
        )

        // 디버그 텍스트(원하면 삭제)
        Text(
            text = "DEBUG: installed=$installed granted=$granted permShown=$permissionDialogShown showProfile=$showProfileDialog",
            color = Color.Red,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .zIndex(999f)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(6.dp)
        )
    }
}

// -----------------------------
// Dialogs + UI Components
// -----------------------------

@Composable
fun SocialLoginProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "필수 정보를 입력해주세요 ✍️",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    "기본 정보 입력 (필수)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "서비스 이용을 위해 기본 정보가 필요합니다:",
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "알림 기능 활성화 🔔",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("보호자 이메일을 함께 등록하면:", fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("✅ 복약 알림 기능 사용 가능", fontSize = 14.sp)
                Text("✅ 미복용 시 보호자에게 알림 전송", fontSize = 14.sp)
                Text("✅ 더 안전한 복약 관리", fontSize = 14.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("입력하기")
            }
        },
        dismissButton = null
    )
}

@Composable
fun NormalLoginGuardianDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "알림 기능을 사용하시겠어요? 🔔",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text("보호자 이메일을 등록하면:", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("✅ 복약 알림 기능 사용 가능", fontSize = 14.sp)
                Text("✅ 미복용 시 보호자에게 알림 전송", fontSize = 14.sp)
                Text("✅ 더 안전한 복약 관리", fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "나중에 프로필에서 언제든 등록할 수 있어요!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("지금 등록하기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("나중에")
            }
        }
    )
}

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
