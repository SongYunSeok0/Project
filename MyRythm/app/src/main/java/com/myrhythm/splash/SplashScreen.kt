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
import androidx.compose.ui.res.stringResource

// 1127 자동로그인 적용 + 스플래시 화면
@Composable
fun SplashScreen(
    onFinish: () -> Unit = {}
) {
    val splashScreenDescription = stringResource(R.string.splashscreen)

    var imageIndex by remember { mutableStateOf(0) }
    val images = listOf(
        R.drawable.splashlogo1,
        R.drawable.splashlogo2,
        R.drawable.splashlogo3
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[imageIndex]),
            contentDescription = splashScreenDescription,
            modifier = Modifier.size(300.dp)
        )
    }

    // 스플래시 화면 - 1초 간격으로 이미지 변경 + 3초 뒤 종료 콜백 호출
    // unit이 한번만실행? true도 동일한거같긴함
    LaunchedEffect(Unit) {
        repeat(3) { i ->
            Log.e("SplashScreen", "⏳ 이미지 index = $i")
            imageIndex = i
            delay(1000)
        }
        Log.e("SplashScreen", "⏳ 3초 끝 → onFinish() 호출")
        onFinish()
    }
}
