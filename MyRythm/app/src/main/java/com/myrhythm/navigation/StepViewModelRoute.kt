package com.myrhythm.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.myrhythm.health.StepViewModel
import com.myrhythm.viewmodel.MainViewModel
import com.shared.ui.MainScreen

@Composable
fun StepViewModelRoute(
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
) {
    val context = LocalContext.current
    val stepViewModel: StepViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()

    val nextTime by mainViewModel.nextTime.collectAsStateWithLifecycle()
    val remainText by mainViewModel.remainText.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.e("HC", "SDK STATUS = $status")
    }

    val installed =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    if (!installed) {
        Toast.makeText(context, "Health Connect 설치 필요", Toast.LENGTH_LONG).show()

        val url =
            "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"
        val installIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(installIntent)

        return
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(stepViewModel.requestPermissions())) {
            stepViewModel.checkPermission()
        } else {
            Toast.makeText(context, "걸음수 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val granted by stepViewModel.permissionGranted.collectAsStateWithLifecycle()
    val todaySteps by stepViewModel.todaySteps.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        stepViewModel.checkPermission()
    }

    // 🔥 중복 실행 방지 버전
    LaunchedEffect(granted) {
        if (!granted) {
            permissionLauncher.launch(stepViewModel.requestPermissions())
        } else {
            stepViewModel.startAutoUpdateOnce(intervalMillis = 5_000L)
        }
    }

    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,
        todaySteps = todaySteps,
        nextTime = nextTime,
        remainText = remainText,
    )
}