package com.myrhythm.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.shared.R
import android.util.Log

@Composable
fun SplashScreen(
    onFinish: () -> Unit = {}
) {

    Log.e("SplashScreen", "🔥 Splash 화면 등장")

    var imageIndex by remember { mutableStateOf(0) }

    // 이미지 리스트
    val images = listOf(
        R.drawable.splashlogo1,
        R.drawable.splashlogo2,
        R.drawable.splashlogo3
    )

    // ✔ 1초 간격으로 이미지 변경 + 3초 뒤 종료 콜백 호출
    LaunchedEffect(Unit) {
        Log.e("SplashScreen", "⏳ 이미지 변경 루프 시작")

        repeat(3) { i ->
            imageIndex = i
            Log.e("SplashScreen", "⏳ 이미지 index = $i")
            delay(1000)
        }

        Log.e("SplashScreen", "⏳ 3초 끝 → onFinish() 호출")
        onFinish()   // 끝나면 네비게이션 호출
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[imageIndex]),
            contentDescription = "스플래시 이미지",
            modifier = Modifier.size(300.dp)
        )
    }
}
