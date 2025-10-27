package com.example.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.common.design.R
import com.sesac.design.ui.theme.AuthOnPrimary
import com.sesac.design.ui.theme.AuthSecondrayButton
import com.sesac.design.ui.theme.AuthOnSecondray
import com.sesac.design.ui.theme.AuthAppName
import com.sesac.design.ui.theme.AuthBackground

val ButtonDisabled = AuthSecondrayButton.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(modifier: Modifier = Modifier) {
    // 상태 관리
    var isPhoneVerificationSent by remember { mutableStateOf(false) }
    var isVerificationCompleted by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(AuthBackground)
                .padding(horizontal = 24.dp, vertical = 30.dp)
        ) {
            // 로고
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MyRhythm Logo Icon",
                modifier = Modifier
                    .requiredSize(size = 120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. 이름 입력 필드 (하드코딩)
            Card(
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)
                ) {
                    Text(
                        text = "김이름",
                        color = AuthOnPrimary,
                        fontSize = 15.sp,
                        letterSpacing = 0.9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 생년월일 섹션
            Text(
                text = "생년월일",
                color = AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 년도 필드
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1.5f).height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "1995년",
                            color = Color.Black,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 월 필드
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "1월",
                            color = Color.Black,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 일 필드
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "1일",
                            color = Color.Black,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. 키 / 몸무게 필드
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 키 필드
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 21.dp)
                    ) {
                        Text(text = "170", color = AuthOnPrimary, fontSize = 15.sp)
                    }
                }
                // 몸무게 필드
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 21.dp)
                    ) {
                        Text(text = "70", color = AuthOnPrimary, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. 전화번호 인증 섹션
            Text(
                text = "전화번호 인증 *",
                color = AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // 전화번호 입력 및 전송 버튼
            Card(
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 28.dp, end = 16.dp)
                ) {
                    Text(
                        text = "010-1111-1111",
                        color = AuthOnPrimary,
                        fontSize = 15.sp,
                        letterSpacing = 0.9.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(40.dp)
                            .background(
                                color = if (isPhoneVerificationSent) ButtonDisabled else AuthSecondrayButton,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !isPhoneVerificationSent) {
                                isPhoneVerificationSent = true
                            }
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (isPhoneVerificationSent) "전송됨" else "전송",
                            color = AuthOnSecondray,
                            fontSize = 14.sp,
                            letterSpacing = 0.84.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 인증번호 입력 및 인증 버튼
            Card(
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 28.dp, end = 16.dp)
                ) {
                    Text(
                        text = "111111",
                        color = AuthOnPrimary,
                        fontSize = 15.sp,
                        letterSpacing = 0.9.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isPhoneVerificationSent) {
                        Text(
                            text = "02:53",
                            color = AuthSecondrayButton,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(40.dp)
                            .background(
                                color = AuthSecondrayButton,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isVerificationCompleted = true
                            }
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "인증",
                            color = AuthOnSecondray,
                            fontSize = 14.sp,
                            letterSpacing = 0.84.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. 프로필 설정 완료 버튼
            Button(
                onClick = { /* 프로필 설정 완료 */ },
                colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            ) {
                Text(
                    text = "프로필 설정 완료",
                    color = AuthOnSecondray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.6.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. 나중에 작성하기
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* 나중에 작성하기 동작 */ }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "나중에 작성하기",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.84.sp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "(일부 기능 제한)",
                    color = Color(0xff7cc8e4),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.84.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    SignupScreen()
}