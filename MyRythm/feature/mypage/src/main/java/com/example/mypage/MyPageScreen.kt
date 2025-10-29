package com.example.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R

@Composable
fun MyPageScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)
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
                Text(text = "안녕하세요", style = TextStyle(fontSize = 14.sp), color = Color(0xff221f1f))
                Text(text = "김이름님", style = TextStyle(fontSize = 16.sp), color = Color(0xff221f1f))
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            InfoCard("Heart rate", "215bpm", R.drawable.heart)
            InfoCard("Height", "170cm", R.drawable.height)
            InfoCard("Weight", "103lbs", R.drawable.weight)
        }

        Spacer(Modifier.height(32.dp))

        Column(Modifier.fillMaxWidth()) {
            MenuItem("내 정보 수정")
            MenuItem("심박수")
            MenuItem("복약 그래프")
            MenuItem("FAQ 문의사항")
            MenuItem("로그아웃")
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, iconRes: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .shadow(1.dp, RoundedCornerShape(6.dp))
            .padding(vertical = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(Color(0xff6ae0d9).copy(alpha = 0.2f))
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(text = value, color = Color(0xff6ae0d9), fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = title, color = Color(0xff5db0a8).copy(alpha = 0.74f), fontSize = 12.sp)
    }
}

@Composable
fun MenuItem(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(64.dp).clickable { }.padding(horizontal = 0.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape)
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
