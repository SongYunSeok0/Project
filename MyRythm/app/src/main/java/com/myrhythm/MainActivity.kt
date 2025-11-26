package com.myrhythm

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import com.myrhythm.splash.SplashScreen
import com.myrhythm.splash.SplashState
import com.myrhythm.splash.SplashViewModel
import com.myrhythm.ui.theme.MyRhythmTheme
import com.myrhythm.viewmodel.InitFcmTokenViewModel
import com.myrythm.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val fcmViewModel: InitFcmTokenViewModel by viewModels()

    // 1126 스플래시뷰모델 추가
    private val splashVm: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1126 17:52
        //splashVm.state.value

        fcmViewModel.initFcmToken()
        askNotificationPermission()

        //setContent { AppRoot() }
        // 1126 setContent 코드 수정
        setContent {
            MyRhythmTheme {

                val splashState by splashVm.state.collectAsState()

                when (splashState) {
                    is SplashState.Loading -> {
                        SplashScreen(
                            onFinish = { splashVm.checkAutoLogin() }
                        )
                    }

                    is SplashState.GoLogin -> {
                        AppRoot(startFromLogin = true)
                    }

                    is SplashState.GoMain -> {
                        AppRoot(startFromLogin = false)
                    }
                }
                    /*
                when (splashState) {
                    SplashState.Loading -> SplashScreen(
                        onFinish = { splashVm.checkAutoLogin() }
                    )

                    SplashState.GoLogin -> AppRoot(startFromLogin = true)

                    SplashState.GoMain -> AppRoot(startFromLogin = false)
                }*/
            }
        }
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

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRhythmTheme { AppRoot() }
}


