package com.scheduler.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R

private val Mint = Color(0xFF6AE0D9)

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onTakePhoto: () -> Unit = {},                 // ← OCR로 이동
    onPickFromGallery: (Uri?) -> Unit = {}        // ← 선택 결과 전달(원하면 OCR로 이동)
) {
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onPickFromGallery(uri) }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 프리뷰 박스
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9F9F9),
                border = BorderStroke(1.66.dp, Mint),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(459.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(456.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            colorFilter = ColorFilter.tint(Mint)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 34.dp)
                            .width(277.dp)
                            .height(392.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.66.dp, Color.White), RoundedCornerShape(10.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .width(243.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Text(
                            text = "처방전을 프레임 안에 맞춰주세요",
                            color = Color(0xFF3B566E),
                            textAlign = TextAlign.Center,
                            lineHeight = 1.43.em,
                            style = TextStyle(fontSize = 14.sp),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // 가이드 카드
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4F5F4))
                    .padding(16.dp)
            ) {
                Text("📋 촬영 가이드", color = Color(0xFF5DB0A8), fontSize = 14.sp)
                Text("• 밝은 곳에서 촬영하면 인식률이 높아집니다", color = Color(0xFF6F8BA4), fontSize = 14.sp)
                Text("• 글씨가 선명하게 보이는지 확인해주세요", color = Color(0xFF6F8BA4), fontSize = 14.sp)
            }

            // 버튼 영역
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {

                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(1.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("사진 촬영", color = Color.White, fontSize = 16.sp)
                }

                OutlinedButton(
                    onClick = { galleryPicker.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(1.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Mint
                    ),
                    border = BorderStroke(1.66.dp, Mint)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Mint)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("갤러리에서 선택", fontSize = 16.sp)
                }
            }
        }
    }
}


@Preview(widthDp = 392, heightDp = 917, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CameraScreenPreview() {
    CameraScreen()
}
