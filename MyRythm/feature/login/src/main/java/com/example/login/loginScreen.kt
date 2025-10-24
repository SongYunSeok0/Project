package com.example.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api // ⬅️ 추가
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.common.design.R // ⬅️ R.drawable 접근을 위해 필요할 수 있습니다.

@OptIn(ExperimentalMaterial3Api::class) // ⬅️ CenterAlignedTopAppBar 경고 해제
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredWidth(width = 412.dp)
            .requiredHeight(height = 917.dp)
            .background(color = Color(0xff6ae0d9))
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(width = 412.dp)
                .requiredHeight(height = 917.dp)
                .background(color = Color(0xff6ae0d9))
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(size = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredSize(size = 0.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 128.26.dp,
                                y = 106.64.dp)
                            .requiredWidth(width = 87.dp)
                            .requiredHeight(height = 85.dp))
                }
                Spacer(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 192.9.dp,
                            y = 125.94.dp)
                        .requiredWidth(width = 16.dp)
                        .requiredHeight(height = 16.dp))
            }
            Surface(
                // shape = undefined 대신 shape 파라미터 자체를 제거하거나 기본값 설정
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 135.dp,
                        y = 60.dp)
                    .rotate(degrees = 34.67f)
                    .shadow(elevation = 15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 132.dp)
                        .requiredHeight(height = 57.dp)
                ) {
                    Image(
                        // ⬅️ 경로 수정
                        painter = painterResource(id = com.example.common.design.R.drawable.ic_android_black_24dp),
                        contentDescription = "Group 53",
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 58.11.dp,
                                y = 39.24.dp)
                            .requiredWidth(width = 22.dp)
                            .requiredHeight(height = 21.dp)
                            .rotate(degrees = -29.81f)
                            .shadow(elevation = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape = CircleShape)
                            .background(color = Color.Black)
                            .shadow(elevation = 4.dp,
                                shape = CircleShape))
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 132.dp)
                            .requiredHeight(height = 57.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 54.37.dp,
                                    y = 0.dp)
                                .requiredWidth(width = 66.dp)
                                .requiredHeight(height = 57.dp)
                                .rotate(degrees = 180f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.White))
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.White))
                        }
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = 37.61.dp)
                                .requiredWidth(width = 66.dp)
                                .requiredHeight(height = 57.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color(0xff6ac1e0)))
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color(0xff6ac1e0)))
                        }
                    }
                }
            }
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Rhythm",
                        color = Color(0xffc9f8f6),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 65.sp))
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = (-19).dp,
                        y = 191.dp))
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 0.dp,
                        y = 334.99.dp)
                    .requiredWidth(width = 413.dp)
                    .requiredHeight(height = 398.dp)
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
                        .shadow(elevation = 4.dp,
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
                        .shadow(elevation = 4.dp,
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
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 131.37.dp,
                            y = 184.91.dp)
                        .requiredWidth(width = 150.dp)
                        .requiredHeight(height = 16.dp)
                ) {
                    Text(
                        text = "비밀번호를 잊으셨나요?",
                        color = Color(0xff77a3a1),
                        lineHeight = 1.16.em,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.84.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 0.dp,
                                y = 0.26.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 84.79.dp,
                            y = 236.97.dp)
                        .requiredWidth(width = 243.dp)
                        .requiredHeight(height = 62.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(color = Color(0xff6ac0e0))
                        .padding(end = 0.0000152587890625.dp)
                        .shadow(elevation = 4.dp,
                            shape = RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 105.dp)
                            .requiredHeight(height = 40.dp)
                    ) {
                        Text(
                            text = "LogIn",
                            color = Color.White,
                            lineHeight = 1.16.em,
                            style = TextStyle(
                                fontSize = 35.sp,
                                letterSpacing = 2.1.sp),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = 1.81.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 0.dp,
                            y = 369.95.dp)
                        .requiredWidth(width = 413.dp)
                        .requiredHeight(height = 28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 226.39.dp,
                                y = 0.dp)
                            .clip(shape = RoundedCornerShape(8.dp))
                            .shadow(elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 87.dp)
                                .requiredHeight(height = 28.dp)
                        ) {
                            Text(
                                text = "회원가입",
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
                            .offset(x = 99.35.dp,
                                y = 3.77.dp)
                            .requiredWidth(width = 127.dp)
                            .requiredHeight(height = 20.dp)
                    ) {
                        Text(
                            text = "계정이 없으신가요?",
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
private fun LoginScreenPreview() {
    LoginScreen(Modifier)
}