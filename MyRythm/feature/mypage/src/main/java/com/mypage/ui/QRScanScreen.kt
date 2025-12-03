package com.mypage.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback


@Composable
fun QRScanScreen(
    onScanSuccess: (uuid: String, token: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var scanError by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    // 카메라 권한
    val cameraPermission = Manifest.permission.CAMERA
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) scanError = "카메라 권한이 필요해!"
        }

    // 첫 진입 시 권한 요청
    LaunchedEffect(Unit) {
        cameraLauncher.launch(cameraPermission)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val scannerView = CodeScannerView(ctx)
                val scanner = CodeScanner(ctx, scannerView)

                scanner.decodeCallback = DecodeCallback { result ->
                    val raw = result.text ?: ""

                    try {
                        val uri = Uri.parse(raw)
                        val uuid = uri.getQueryParameter("uuid") ?: ""
                        val token = uri.getQueryParameter("token") ?: ""

                        if (uuid.isNotBlank() && token.isNotBlank()) {
                            onScanSuccess(uuid, token)
                        } else {
                            scanError = "올바른 QR 코드가 아니야!"
                        }
                    } catch (e: Exception) {
                        scanError = "QR 해석 실패!"
                    }
                }

                scanner.setErrorCallback { error ->
                    scanError = "카메라 초기화 실패: ${error.message ?: ""}"
                }

                scanner.startPreview()
                scannerView
            }
        )

        Text(
            text = "뒤로가기",
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clickable { onBack() }
        )

        scanError?.let {
            Text(
                it,
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

