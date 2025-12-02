package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.mypage.viewmodel.MyPageEvent
import com.shared.R
import com.mypage.viewmodel.MyPageViewModel
import com.shared.ui.components.ProfileHeader

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onEditClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onMediClick: () -> Unit = {},
    onWithdrawalSuccess: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }


    var showDeviceDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPageEvent.WithdrawalSuccess -> {
                    Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    onWithdrawalSuccess()
                }
                is MyPageEvent.WithdrawalFailed -> {
                    Toast.makeText(context, "탈퇴 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                is MyPageEvent.LogoutSuccess -> {
                    Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    onLogoutClick()
                }
                is MyPageEvent.LogoutFailed -> {
                    Toast.makeText(context, "로그아웃에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                else -> {} // 다른 이벤트 무시
            }
        }
    }

    val editPageText = stringResource(R.string.editpage)
    val heartRateText = stringResource(R.string.heartrate)
    val faqCategoryText = stringResource(R.string.faqcategory)
    val logoutText = stringResource(R.string.logout)

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
            InfoCard("Heart rate", "215bpm", R.drawable.heart)
            InfoCard("Height", "${profile?.height ?: "-"}cm", R.drawable.height)
            InfoCard("Weight", "${profile?.weight ?: "-"}kg", R.drawable.weight)
        }

        Spacer(Modifier.height(32.dp))

        Column(Modifier.fillMaxWidth()) {
            MenuItem(editPageText, onEditClick)
            MenuItem(heartRateText, onHeartClick)
            MenuItem("복약 기록",onMediClick)
            MenuItem("기기 등록") {showDeviceDialog = true}
            MenuItem(faqCategoryText, onFaqClick)
            MenuItem(logoutText) { viewModel.logout() }
            MenuItem("회원 탈퇴") { showDeleteDialog = true }
        }
    }

    if (showDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceDialog = false },

            title = { Text("기기 등록") },
            text = { Text("정말 기기를 등록하시겠습니까?") },

            confirmButton = {
                Text(
                    text = "등록하기",
                    color = Color(0xFF407CE2),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showDeviceDialog = false
                            viewModel.requestDeviceRegister()
                        }
                )
            },
            dismissButton = {
                Text(
                    text = "취소",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { showDeviceDialog = false }
                )
            }
        )
    }


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("정말 탈퇴하시겠습니까?")
            },
            text = {
                Text("회원 탈퇴 시 모든 데이터가 삭제되며\n복구가 불가능합니다.")
            },
            confirmButton = {
                Text(
                    text = "탈퇴하기",
                    color = Color.Red,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showDeleteDialog = false
                            viewModel.deleteAccount()
                        }
                )
            },
            dismissButton = {
                Text(
                    text = "취소",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showDeleteDialog = false
                        }
                )
            }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    iconRes: Int
) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                color = Color(0xFF4CCDC5),
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MenuItem(title: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xff407ce2).copy(alpha = 0.13f))
        )
        Spacer(Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, color = Color(0xff221f1f))
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.arrow),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(widthDp = 392, heightDp = 1271)
@Composable
private fun MyPageScreenPreview() {
    MyPageScreen()
}