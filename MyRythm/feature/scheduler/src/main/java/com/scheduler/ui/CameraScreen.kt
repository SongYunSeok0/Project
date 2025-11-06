package com.scheduler.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image // ✅ 이 Image만 사용
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.scheduler.ocr.CameraActivity
import java.io.File
import java.io.FileOutputStream

private val Mint = Color(0xFF6AE0D9)

/** ⚠️ 파일 내에 이 시그니처의 CameraScreen이 1개만 존재해야 함 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onOpenOcr: (String) -> Unit = {},
    onOpenRegi: () -> Unit = {},          // ✅ 기본값 제공해서 호출부/프리뷰 충돌 제거
) {
    val context = LocalContext.current

    // CameraActivity → RESULT_OK 로 imagePath 회수
    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra("imagePath") ?: return@rememberLauncherForActivityResult
            onOpenOcr(path)
        }
    }

    // 갤러리 Uri → 캐시 복사 → 경로 전달
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val path = copyUriToCache(context, uri) ?: return@rememberLauncherForActivityResult
        onOpenOcr(path)
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(5.dp))

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
                Image(
                    painter = painterResource(id = R.drawable.example),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Text("• 밝은 곳에서 촬영", color = Color(0xFF6F8BA4), fontSize = 14.sp)
                Text("• 글씨 선명도 확인", color = Color(0xFF6F8BA4), fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 사진 촬영
                Button(
                    onClick = { takePhotoLauncher.launch(Intent(context, CameraActivity::class.java)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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

                // 갤러리
                OutlinedButton(
                    onClick = { galleryPicker.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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

                // 수동 입력 → RegiScreen
                Button(
                    onClick = onOpenRegi,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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
                    Text("수동 입력", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

/** 헬퍼는 파일에서 단 한 번만 선언 */
private fun copyUriToCache(context: Context, uri: Uri): String? = try {
    val name = "pick_${System.currentTimeMillis()}.jpg"
    val outFile = File(context.cacheDir, name)
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(outFile).use { output -> input.copyTo(output) }
    }
    outFile.absolutePath
} catch (_: Exception) { null }

@Preview(widthDp = 392, heightDp = 917, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CameraScreenPreview() {
    CameraScreen() // 기본값들로 미리보기 가능
}
