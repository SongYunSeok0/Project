package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.BLERegisterViewModel
import com.mypage.viewmodel.MyPageEvent
import com.mypage.viewmodel.MyPageViewModel
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.ProfileHeader
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    bleViewModel: BLERegisterViewModel = hiltViewModel(),
    onEditClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onMediClick: () -> Unit = {},
    onDeviceRegisterClick: () -> Unit = {},   // ⭐ 추가
    onWithdrawalSuccess: () -> Unit = {}
) {
    val editPageText = stringResource(R.string.editpage)
    val heartRateText = stringResource(R.string.heartrate)
    val mediRecordText = stringResource(R.string.medi_record)
    val deviceRegisterText = stringResource(R.string.device_register)
    val faqCategoryText = stringResource(R.string.faqcategory)
    val logoutText = stringResource(R.string.logout)
    val withdrawalText = stringResource(R.string.withdrawal)
    val cancelText = stringResource(R.string.cancel)
    val withdrawalConfirmText = stringResource(R.string.withdrawal_confirm)
    val wifiSsidText = stringResource(R.string.wifi_ssid)
    val wifiPasswordText = stringResource(R.string.wifi_password)
    val wifiConnectingText = stringResource(R.string.wifi_connecting)
    val registerText = stringResource(R.string.register)
    val cmText = stringResource(R.string.cm)
    val kgText = stringResource(R.string.kg)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val bpmText = stringResource(R.string.bpm)
    val withdrawalTitleMessage = stringResource(R.string.mypage_message_withdrawal_title)
    val withdrawalMessage = stringResource(R.string.mypage_message_withdrawal)
    val deviceRegisterMessage = stringResource(R.string.mypage_message_device_register)
    val wifiConfigSentMessage = stringResource(R.string.mypage_message_wifi_config_sent)
    val wifiBleConnectedSuccessMessage = stringResource(R.string.mypage_message_wifi_ble_connected_success)
    val logoutSuccessMessage = stringResource(R.string.mypage_message_logout_success)
    val logoutFailedMessage = stringResource(R.string.mypage_message_logout_failed)
    val withdrawalSuccessMessage = stringResource(R.string.mypage_message_withdrawal_success)
    val withdrawalFailedMessage = stringResource(R.string.mypage_message_withdrawal_failed)

    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current

    val bleState by bleViewModel.state.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }

    // ============================================================
    // ⭐ QRScanScreen → MyPage 복귀 후 deviceUUID/deviceToken 들어오면 팝업 자동 오픈
    // ============================================================
    LaunchedEffect(bleState.deviceUUID, bleState.deviceToken) {
        if (bleState.deviceUUID.isNotBlank() && bleState.deviceToken.isNotBlank()) {
            showDeviceDialog = true
        }
    }

    // BLE 상태 변화 → 토스트 표시
    LaunchedEffect(bleState.bleConnected, bleState.configSent, bleState.error) {
        when {
            bleState.error != null -> {
                Toast.makeText(context, bleState.error ?: logoutFailedMessage, Toast.LENGTH_SHORT).show()
            }
            bleState.configSent -> {
                Toast.makeText(context, wifiConfigSentMessage, Toast.LENGTH_SHORT).show()
            }
            bleState.bleConnected -> {
                Toast.makeText(context, wifiBleConnectedSuccessMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 기존 MyPage 이벤트 수집
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPageEvent.WithdrawalSuccess -> {
                    Toast.makeText(context, withdrawalSuccessMessage, Toast.LENGTH_SHORT).show()
                    onWithdrawalSuccess()
                }
                is MyPageEvent.WithdrawalFailed -> {
                    Toast.makeText(context, withdrawalFailedMessage, Toast.LENGTH_SHORT).show()
                }
                is MyPageEvent.LogoutSuccess -> {
                    Toast.makeText(context, logoutSuccessMessage, Toast.LENGTH_SHORT).show()
                    onLogoutClick()
                }
                is MyPageEvent.LogoutFailed -> {
                    Toast.makeText(context, logoutFailedMessage, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    // ==================== UI ====================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        ProfileHeader(username = profile?.username)
        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoCard(heartRateText, "215 $bpmText", R.drawable.heart)
            InfoCard(heightText, "${profile?.height ?: "-"} $cmText", R.drawable.height)
            InfoCard(weightText, "${profile?.weight ?: "-"} $kgText", R.drawable.weight)
        }

        Spacer(Modifier.height(32.dp))

        Column(Modifier.fillMaxWidth()) {
            MenuItem(editPageText, R.drawable.edit, onEditClick, tint = MaterialTheme.componentTheme.completionCaution)
            MenuItem(heartRateText, R.drawable.rate, onHeartClick)
            MenuItem(mediRecordText, R.drawable.logo, onMediClick)

            // 🔥 “기기 등록" → QRScanRoute로 이동
            MenuItem(deviceRegisterText, R.drawable.device, { onDeviceRegisterClick() } )

            MenuItem(faqCategoryText, R.drawable.faqchat, onFaqClick)
            MenuItem(logoutText, R.drawable.logout, { viewModel.logout() } )
            MenuItem(withdrawalText, R.drawable.ic_delete, { showDeleteDialog = true }, tint = MaterialTheme.colorScheme.onSurface)
        }
    }

    // ==================== BLE 기기 등록 다이얼로그 ====================
    if (showDeviceDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { showDeviceDialog = false },
            title = { Text(deviceRegisterText) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        deviceRegisterMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                        )
                    AppInputField(
                        value = bleState.ssid,
                        onValueChange = { bleViewModel.updateSSID(it) },
                        label = wifiSsidText,
                        outlined = true,
                        singleLine = true
                    )
                    AppInputField(
                        value = bleState.pw,
                        onValueChange = { bleViewModel.updatePW(it) },
                        label = wifiPasswordText,
                        outlined = true,
                        singleLine = true
                    )
                    if (bleState.loading) {
                        Text(
                            wifiConnectingText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = registerText,
                    height = 40.dp,
                    width = 100.dp,
                    onClick = {
                        bleViewModel.startRegister()
                        showDeviceDialog = false
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = cancelText,
                    height = 40.dp,
                    width = 70.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showDeviceDialog = false }
                )
            }
        )
    }

    // ==================== 회원 탈퇴 다이얼로그 ====================
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(withdrawalTitleMessage) },
            text = {
                Text(
                    withdrawalMessage,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                AppButton(
                    text = withdrawalConfirmText,
                    height = 40.dp,
                    width = 100.dp,
                    backgroundColor = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.onError,
                    onClick = { viewModel.deleteAccount()
                        showDeleteDialog = false
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = cancelText,
                    height = 40.dp,
                    width = 70.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showDeleteDialog = false }
                )
            }
        )
    }
}

@Composable
fun InfoCard(title: String, value: String, iconRes: Int) {
    AppTheme {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    tint: Color? = null,
    ) {
    val arrowText = stringResource(R.string.arrow_description)
    AppTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (tint != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.arrow),
                contentDescription = arrowText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Preview_AllDialogs_Latest() {

    AppTheme {

        // ====================== 프리뷰용 가짜 상태 ======================
        var showDeviceDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        // 기기 등록 다이얼로그용 가짜 BLE 상태
        var fakeSSID by remember { mutableStateOf("") }
        var fakePW by remember { mutableStateOf("") }
        var fakeLoading by remember { mutableStateOf(true) }

        // 문자열 리소스 대체 프리뷰용 텍스트
        val deviceRegisterText = "기기 등록"
        val deviceRegisterMessage = "Wi-Fi 정보를 입력하면\n기기에 BLE로 전송됩니다."
        val wifiSsidText = "Wi-Fi SSID"
        val wifiPasswordText = "Wi-Fi Password"
        val wifiConnectingText = "기기 연결 중..."
        val registerText = "등록하기"
        val cancelText = "취소"

        val withdrawalTitleMessage = "회원 탈퇴"
        val withdrawalMessage = "정말 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다."
        val withdrawalConfirmText = "탈퇴하기"

        // ====================== 프리뷰 배경 UI ======================
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text("다이얼로그 프리뷰", fontSize = 22.sp)
                Spacer(Modifier.height(20.dp))

                // 기기 등록 버튼
                AppButton(
                    text = "기기 등록 다이얼로그 열기",
                    height = 48.dp,
                    onClick = { showDeviceDialog = true }
                )
                Spacer(Modifier.height(16.dp))

                // 회원 탈퇴 버튼
                AppButton(
                    text = "회원 탈퇴 다이얼로그 열기",
                    height = 48.dp,
                    backgroundColor = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.onError,
                    onClick = { showDeleteDialog = true }
                )
            }

            // =====================================================================
            //                      ⭐ 1) 기기 등록 다이얼로그
            // =====================================================================
            if (showDeviceDialog) {
                AlertDialog(
                    containerColor = MaterialTheme.colorScheme.background,
                    onDismissRequest = { showDeviceDialog = false },

                    title = { Text(deviceRegisterText) },

                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                deviceRegisterMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // === AppInputField 적용 ===
                            AppInputField(
                                value = fakeSSID,
                                onValueChange = { fakeSSID = it },
                                label = wifiSsidText,
                                outlined = true,
                                singleLine = true
                            )

                            AppInputField(
                                value = fakePW,
                                onValueChange = { fakePW = it },
                                label = wifiPasswordText,
                                outlined = true,
                                singleLine = true
                            )

                            if (fakeLoading) {
                                Text(
                                    wifiConnectingText,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },

                    confirmButton = {
                        AppButton(
                            text = registerText,
                            height = 40.dp,
                            width = 100.dp,
                            onClick = {
                                fakeLoading = true
                                showDeviceDialog = false
                            }
                        )
                    },

                    dismissButton = {
                        AppButton(
                            text = cancelText,
                            height = 40.dp,
                            width = 70.dp,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { showDeviceDialog = false }
                        )
                    }
                )
            }

            // =====================================================================
            //                      ⭐ 2) 회원 탈퇴 다이얼로그
            // =====================================================================
            if (showDeleteDialog) {
                AlertDialog(
                    containerColor = MaterialTheme.colorScheme.background,
                    onDismissRequest = { showDeleteDialog = false },

                    title = { Text(withdrawalTitleMessage) },

                    text = {
                        Text(
                            withdrawalMessage,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },

                    confirmButton = {
                        AppButton(
                            text = withdrawalConfirmText,
                            height = 40.dp,
                            width = 100.dp,
                            backgroundColor = MaterialTheme.colorScheme.error,
                            textColor = MaterialTheme.colorScheme.onError,
                            onClick = { showDeleteDialog = false }
                        )
                    },

                    dismissButton = {
                        AppButton(
                            text = cancelText,
                            height = 40.dp,
                            width = 70.dp,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { showDeleteDialog = false }
                        )
                    }
                )
            }
        }
    }
}
