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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwdScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredWidth(width = 412.dp)
            .requiredHeight(height = 917.dp)
            .background(color = Color(0xffb5e5e1))
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 412.dp)
                .requiredHeight(height = 917.dp)
                .background(color = Color(0xffb5e5e1))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 80.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "MyRhythm Logo Icon",
                    modifier = Modifier
                        .requiredSize(size = 120.dp)
                        .clip(CircleShape)
                )
                Image(
                    painter = painterResource(id = R.drawable.login_myrhythm),
                    contentDescription = "My Rhythm Text Logo",
                    modifier = Modifier
                        .padding(top = 16.dp) // 아이콘과의 간격
                        .requiredWidth(280.dp)
                        .requiredHeight(80.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 0.dp,
                        y = 334.99.dp)
                    .requiredWidth(width = 413.dp)
                    .requiredHeight(height = 337.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 47.29.dp, y = 0.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .requiredWidth(width = 318.dp)
                            .requiredHeight(height = 56.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .padding(start = 28.dp, end = 10.dp)
                    ) {
                        Text(
                            text = "휴대폰 번호",
                            color = Color.Black.copy(alpha = 0.4f),
                            style = TextStyle(
                                fontSize = 15.sp,
                                letterSpacing = 0.9.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .requiredWidth(width = 65.dp)
                                .requiredHeight(height = 40.dp)
                                .clip(shape = RoundedCornerShape(8.dp))
                                .background(color = Color(0xff6ac0e0))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "전송",
                                color = Color.White,
                                lineHeight = 1.5.em,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    letterSpacing = 0.84.sp)
                            )
                        }
                    }
                }

                // 2-2. 인증번호 입력 필드
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
                            .padding(horizontal = 28.dp, vertical = 19.dp)
                    ) {
                        Text(
                            text = "인증번호",
                            color = Color.Black.copy(alpha = 0.4f),
                            style = TextStyle(
                                fontSize = 15.sp,
                                letterSpacing = 0.9.sp))
                    }
                }

                // 2-3. 확인 버튼
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 84.79.dp,
                            y = 195.98.dp)
                        .requiredWidth(width = 243.dp)
                        .requiredHeight(height = 62.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(color = Color(0xff6ac0e0))
                        .padding(end = 0.01155853271484375.dp)
                        .shadow(elevation = 1.dp,
                            shape = RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 69.dp)
                            .requiredHeight(height = 40.dp)
                    ) {
                        Text(
                            text = "확인",
                            color = Color.White,
                            lineHeight = 1.16.em,
                            style = TextStyle(
                                fontSize = 35.sp,
                                letterSpacing = 2.1.sp),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = (-0.41).dp))
                    }
                }

                // 2-4. 로그인 링크
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 0.dp,
                            y = 308.97.dp)
                        .requiredWidth(width = 413.dp)
                        .requiredHeight(height = 28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 249.25.dp,
                                y = 0.dp)
                            .clip(shape = RoundedCornerShape(8.dp))
                            .shadow(elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 73.dp)
                                .requiredHeight(height = 28.dp)
                        ) {
                            Text(
                                text = "로그인",
                                color = Color(0xff6ac0e0),
                                lineHeight = 1.16.em,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    letterSpacing = 0.84.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 16.dp,
                                        y = 5.77.dp))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 90.21.dp,
                                y = 3.77.dp)
                            .requiredWidth(width = 159.dp)
                            .requiredHeight(height = 20.dp)
                    ) {
                        Text(
                            text = "이미 계정이 있으신가요?",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 1.16.em,
                            style = TextStyle(
                                fontSize = 14.sp,
                                letterSpacing = 0.84.sp))
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun PwdScreenPreview() {
    PwdScreen(Modifier)
}