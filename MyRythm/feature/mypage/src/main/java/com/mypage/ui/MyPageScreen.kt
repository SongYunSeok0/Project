package com.mypage.ui

import android.util.Log
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mypage.viewmodel.MyPageUiState
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.ProfileHeader
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme

@Composable
fun MyPageScreen(
    state: MyPageUiState,
    onDeleteAccount: () -> Unit,
    onEditClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onMediClick: () -> Unit = {},
    onDeviceRegisterClick: () -> Unit = {},
    onUserManagementClick: () -> Unit = {},
    onInquiriesManagementClick: () -> Unit = {}
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
    val cmText = stringResource(R.string.cm)
    val kgText = stringResource(R.string.kg)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val bpmText = stringResource(R.string.bpm)
    val withdrawalTitleMessage = stringResource(R.string.mypage_message_withdrawal_title)
    val withdrawalMessage = stringResource(R.string.mypage_message_withdrawal)
    val userManagementText = "사용자 관리"
    val inquiriesManagementText = "문의사항 관리"
    val staffMenuText = "관리자 메뉴"

    val profile = state.profile
    val latestHeartRate = state.latestHeartRate
    val heartRateTextValue = latestHeartRate?.let { "$it $bpmText" } ?: "- $bpmText"
    var showDeleteDialog by remember { mutableStateOf(false) }


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
            InfoCard(heartRateText, heartRateTextValue, R.drawable.heart)
            InfoCard(heightText, "${profile?.height ?: "-"} $cmText", R.drawable.height)
            InfoCard(weightText, "${profile?.weight ?: "-"} $kgText", R.drawable.weight)
        }

        Spacer(Modifier.height(32.dp))

        Column(Modifier.fillMaxWidth()) {
            // 스태프 전용 메뉴
            if (profile?.isStaff == true) {
                Text(
                    text = staffMenuText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                MenuItem(
                    userManagementText,
                    R.drawable.edit,
                    onUserManagementClick,
                    tint = MaterialTheme.colorScheme.primary
                )
                MenuItem(
                    inquiriesManagementText,
                    R.drawable.faqchat,
                    onInquiriesManagementClick,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            // 일반 메뉴
            MenuItem(editPageText, R.drawable.edit, onEditClick, tint = MaterialTheme.componentTheme.completionCaution)
            MenuItem(heartRateText, R.drawable.rate, onHeartClick)
            MenuItem(mediRecordText, R.drawable.logo, onMediClick)
            MenuItem(deviceRegisterText, R.drawable.device, { onDeviceRegisterClick() })

            if (profile?.isStaff != true) {
                MenuItem(faqCategoryText, R.drawable.faqchat, onFaqClick)
            }

            MenuItem(
                logoutText,
                R.drawable.logout,
                {
                    Log.e("MyPageScreen", "🔥🔥🔥 로그아웃 버튼 클릭됨!")
                    onLogoutClick()
                    Log.e("MyPageScreen", "🔥🔥🔥 콜백 호출 완료!")
                }
            )

            MenuItem(withdrawalText, R.drawable.ic_delete, { showDeleteDialog = true }, tint = MaterialTheme.colorScheme.onSurface)
        }
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
                    onClick = {
                        onDeleteAccount()
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