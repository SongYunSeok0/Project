package com.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onMediClick: () -> Unit = {}
) {
    val bpmText = stringResource(R.string.bpm)
    val userText = stringResource(R.string.user)
    val cmText = stringResource(R.string.cm)
    val kgText = stringResource(R.string.kg)
    val editPageText = stringResource(R.string.editpage)
    val heartRateText = stringResource(R.string.heartrate)
    val medicationInsightText = stringResource(R.string.medicationinsight)
    val faqCategoryText = stringResource(R.string.faqcategory)
    val logoutText = stringResource(R.string.logout)
    val profileGreetingMessage = stringResource(R.string.mypage_message_profile_greeting)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xffffb7c5))
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(text = "😊", fontSize = 48.sp) }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    profileGreetingMessage, style = TextStyle(fontSize = 14.sp), color = Color(0xff221f1f))
                Text(text = "김이름 $userText", style = TextStyle(fontSize = 16.sp), color = Color(0xff221f1f))
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            InfoCard("Heart rate", "215 $bpmText", R.drawable.heart)
            InfoCard("Height", "170 $cmText", R.drawable.height)
            InfoCard("Weight", "103 $kgText", R.drawable.weight)
        }

        Spacer(Modifier.height(32.dp))

        Column(Modifier.fillMaxWidth()) {
            MenuItem(editPageText, onEditClick)
            MenuItem(heartRateText, onHeartClick)
            MenuItem(medicationInsightText) { onMediClick }
            MenuItem(faqCategoryText, onFaqClick)
            MenuItem(logoutText, onLogoutClick)
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
                    .background(MaterialTheme.colorScheme.primary
                        .copy(alpha = 0.15f)),
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
        Image(painter = painterResource(id = R.drawable.arrow), contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Preview(widthDp = 392, heightDp = 1271)
@Composable
private fun MyPageScreenPreview() { MyPageScreen() }
