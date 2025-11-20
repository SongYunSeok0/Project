package com.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shared.R
import com.mypage.viewmodel.MyPageViewModel

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),   // 🔥 viewModel 추가
    onEditClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onMediClick: () -> Unit = {}
) {
    // 🔥 프로필 상태 Flow → Compose State
    val profile = viewModel.profile.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xffffb7c5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "😊", fontSize = 48.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(text = "안녕하세요")

                // 🔥 profile.username 표시
                Text(text = "${profile?.username ?: ""}님")
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            InfoCard("Heart rate", "215bpm", R.drawable.heart)

            InfoCard("Height", "${profile?.height ?: "-"}cm", R.drawable.height)

            InfoCard("Weight", "${profile?.weight ?: "-"}kg", R.drawable.weight)
        }

        Spacer(Modifier.height(32.dp))

        Column(Modifier.fillMaxWidth()) {
            MenuItem("내 정보 수정", onEditClick)
            MenuItem("심박수", onHeartClick)
            MenuItem("복약 그래프") { onMediClick }
            MenuItem("FAQ 문의사항", onFaqClick)
            MenuItem("로그아웃", onLogoutClick)
        }
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
            .background(Color.White), // 은은한 배경색

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
                    .background(Color(0xFF6AE0D9).copy(alpha = 0.15f)),
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
                color = Color(0xFF5DB0A8),
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
        Image(painter = painterResource(id = R.drawable.arrow), contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Preview(widthDp = 392, heightDp = 1271)
@Composable
private fun MyPageScreenPreview() { MyPageScreen() }
