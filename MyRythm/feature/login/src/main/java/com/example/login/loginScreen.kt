package com.example.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

import com.example.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredWidth(412.dp)
            .requiredHeight(917.dp)
            .background(color = Color(0xff6ae0d9))
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(412.dp)
                .requiredHeight(917.dp)
                .background(color = Color(0xff6ae0d9))
        ) {

            // 1. 로고 아이콘과 텍스트 이미지를 묶어 상단 중앙에 배치하는 영역 (이미지 활성화)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 80.dp) // 전체 로고 그룹을 상단에서 80dp 아래로 이동
            ) {
                // 1-1. 알약 모양 로고 아이콘 이미지 (R.drawable.logo)
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "MyRhythm Logo Icon",
                    modifier = Modifier
                        .requiredSize(size = 120.dp)
                        .clip(CircleShape)
                )

                // 1-2. My Rhythm 텍스트 이미지 (R.drawable.login_mythythm)
                Image(
                    painter = painterResource(id = R.drawable.login_myrhythm),
                    contentDescription = "My Rhythm Text Logo",
                    modifier = Modifier
                        .padding(top = 16.dp) // 아이콘과의 간격
                        .requiredWidth(280.dp)
                        .requiredHeight(80.dp)
                )
            }

            // ⚠️ 불필요한 CenterAlignedTopAppBar 코드는 제거되었습니다.
            // ⚠️ 임시 Text("My Rhythm LOGO HERE") 코드도 제거되었습니다.

                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 0.dp,
                            y = 334.99.dp)
                        .requiredWidth(width = 413.dp)
                        .requiredHeight(height = 515.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 47.29.dp,
                                y = 0.dp)
                            .requiredWidth(width = 318.dp)
                            .requiredHeight(height = 56.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(color = Color.White)
                            .shadow(elevation = 1.dp,
                                shape = RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(height = 56.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 28.dp,
                                    vertical = 19.dp)
                        ) {
                            Text(
                                text = "아이디",
                                color = Color.Black.copy(alpha = 0.4f),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    letterSpacing = 0.9.sp))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 47.29.dp,
                                y = 88.99.dp)
                            .requiredWidth(width = 318.dp)
                            .requiredHeight(height = 56.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(color = Color.White)
                            .shadow(elevation = 1.dp,
                                shape = RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(height = 56.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 28.dp,
                                    vertical = 19.dp)
                        ) {
                            Text(
                                text = "비밀번호",
                                color = Color.Black.copy(alpha = 0.4f),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    letterSpacing = 0.9.sp))
                        }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 131.37.dp, y = 184.91.dp)
                        .requiredWidth(150.dp)
                        .requiredHeight(16.dp)
                ) {
                    Text(
                        text = "비밀번호를 잊으셨나요?",
                        color = Color(0xff77a3a1),
                        lineHeight = 1.16.em,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.84.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 0.dp, y = 0.26.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 84.79.dp, y = 236.97.dp)
                        .requiredWidth(243.dp)
                        .requiredHeight(62.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xff6ac0e0))
                        .padding(end = 0.0000152587890625.dp)
                        .shadow(1.dp, RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(105.dp)
                            .requiredHeight(40.dp)
                    ) {
                        Text(
                            text = "LogIn",
                            color = Color.White,
                            lineHeight = 1.16.em,
                            style = TextStyle(fontSize = 35.sp, letterSpacing = 2.1.sp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 0.dp, y = 1.81.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 0.dp, y = 369.95.dp)
                        .requiredWidth(413.dp)
                        .requiredHeight(28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 226.39.dp, y = 0.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(87.dp)
                                .requiredHeight(28.dp)
                        ) {
                            Text(
                                text = "회원가입",
                                color = Color(0xff6ac0e0),
                                lineHeight = 1.16.em,
                                style = TextStyle(fontSize = 14.sp, letterSpacing = 0.84.sp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = 16.dp, y = 5.77.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 99.35.dp, y = 3.77.dp)
                            .requiredWidth(127.dp)
                            .requiredHeight(20.dp)
                    ) {
                        Text(
                            text = "계정이 없으신가요?",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 1.16.em,
                            style = TextStyle(fontSize = 14.sp, letterSpacing = 0.84.sp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(Modifier)
}
