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
import com.shared.ui.MainScreen

@Composable
fun StepViewModelRoute(
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenSteps: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onFabCamera: () -> Unit = {},
    vm: StepViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.e("HC", "SDK STATUS = $status")
    }

    // 1) Health Connect 설치 여부 체크
    val installed =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    if (!installed) {
        Toast.makeText(context, "Health Connect 설치 필요", Toast.LENGTH_LONG).show()

        val url =
            "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"
        val installIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(installIntent)

        return     // composable 종료
    }

    // 2) 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(vm.requestPermissions())) {
            vm.checkPermission()
        } else {
            Toast.makeText(context, "걸음수 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val granted by vm.permissionGranted.collectAsStateWithLifecycle()
    val todaySteps by vm.todaySteps.collectAsStateWithLifecycle()

    // 3-1) 최초 한 번 권한 상태 체크
    LaunchedEffect(Unit) {
        vm.checkPermission()
    }

    // 3-2) 권한 상태 변화에 따라 처리
    LaunchedEffect(granted) {
        if (!granted) {
            // 권한 없으면 요청
            permissionLauncher.launch(vm.requestPermissions())
        } else {
            // 🔥 권한 있으면 자동 업데이트 시작
            vm.startAutoUpdate(intervalMillis = 5_000L)
        }
    }

    // 4) UI
    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenSteps = onOpenSteps,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,
        onFabCamera = onFabCamera,
        todaySteps = todaySteps
    )
}
