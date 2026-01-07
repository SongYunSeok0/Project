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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val uiState by myPageViewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    val hasGuardian = profile?.prot_email?.isNotBlank() == true

    var showProfileDialog by remember { mutableStateOf(false) }
    var showExtendDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // 소셜 로그인 vs 일반 로그인 구분
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
            android.util.Log.e("ProfileCheck", "isSocialLogin = $missingBasicInfo")

            ProfileCheckResult(
                isSocialLogin = missingBasicInfo,  // 👈 username/phone으로 판단!
                needsBasicInfo = missingBasicInfo,
                needsGuardian = missingGuardian
            )
        }
    }

    // 프로필 정보 입력 팝업 표시 여부
    LaunchedEffect(profile, profileCheck) {
        android.util.Log.e("ProfileDialog", "========== LaunchedEffect 트리거 ==========")
        android.util.Log.e("ProfileDialog", "profile = $profile")
        android.util.Log.e("ProfileDialog", "profileCheck = $profileCheck")

        val check = profileCheck ?: run {
            android.util.Log.e("ProfileDialog", "❌ profileCheck is null (profile 로드 중)")
            return@LaunchedEffect
        }

        val hasClosedDialog = prefs.getBoolean("closed_profile_dialog", false)
        android.util.Log.e("ProfileDialog", "hasClosedDialog = $hasClosedDialog")

        if (check.isSocialLogin) {
            // 소셜 로그인: 기본 정보 비어있으면 무조건 표시
            android.util.Log.e("ProfileDialog", "✅ 소셜 로그인 → 팝업 표시")
            showProfileDialog = true
        } else if (!hasClosedDialog && check.needsGuardian) {
            // 일반 로그인: 보호자 이메일 없고, 닫은 적 없으면 표시
            android.util.Log.e("ProfileDialog", "✅ 일반 로그인 + 보호자 없음 → 팝업 표시")
            showProfileDialog = true
        } else {
            android.util.Log.e("ProfileDialog", "❌ 팝업 표시 조건 불충족")
        }

        android.util.Log.e("ProfileDialog", "showProfileDialog = $showProfileDialog")
    }

    // 팝업 표시
    if (showProfileDialog && profile != null && profileCheck != null) {
        android.util.Log.e("ProfileDialog", "========== 팝업 렌더링 시작 ==========")
        val check = profileCheck!!
        android.util.Log.e("ProfileDialog", "isSocialLogin = ${check.isSocialLogin}")

        if (check.isSocialLogin) {
            android.util.Log.e("ProfileDialog", "→ SocialLoginProfileDialog 표시")
            SocialLoginProfileDialog(
                onDismiss = {},
                onConfirm = {
                    showProfileDialog = false
                    onOpenEditScreen()
                }
            )
        } else {
            android.util.Log.e("ProfileDialog", "→ NormalLoginGuardianDialog 표시")
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

    val installed = HealthConnectClient.getSdkStatus(context) ==
            HealthConnectClient.SDK_AVAILABLE

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
}

// 소셜 로그인 팝업 (기본 정보 필수)
// profile 을 직접 사용하지 않도록 수정
@Composable
fun SocialLoginProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // 소셜의 경우 외부에서 {} 를 넘기면 뒤로가기/밖 터치 막힘
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
        dismissButton = null // "나중에" 버튼 없음
    )
}

// 일반 로그인 팝업 (보호자 이메일 선택)
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
