package com.myrhythm

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.kakao.sdk.common.util.Utility
import com.myrhythm.ui.theme.MyRhythmTheme
import com.myrhythm.viewmodel.InitFcmTokenViewModel
import com.myrythm.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val fcmViewModel: InitFcmTokenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fcmViewModel.initFcmToken()
        askNotificationPermission()

        setContent { AppRoot() }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(perm), 1001)
            }
        }
    }
}
/*1125 기존의스플래시코드 복붙만
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()    // 스플래시 api + 의존성 추가
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FigmatestTheme {
                var showSplash by remember { mutableStateOf(true) }

                // 3초 뒤 AppNavigation으로 전환
                LaunchedEffect(Unit) {
                    delay(3000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreenContent()
                } else {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun SplashScreenContent() {
    var imageIndex by remember { mutableStateOf(0) }

    // 1초 간격으로 이미지 변경
    LaunchedEffect(Unit) {
        repeat(3) { i ->
            imageIndex = i
            delay(1000)
        }
    }

    val images = listOf(
        R.drawable.splashlogo1,
        R.drawable.splashlogo2,
        R.drawable.splashlogo3
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6ae0d9)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[imageIndex]),
            contentDescription = "스플래시 이미지",
            modifier = Modifier.size(300.dp)
        )
    }
}
 */



@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRhythmTheme { AppRoot() }
}


