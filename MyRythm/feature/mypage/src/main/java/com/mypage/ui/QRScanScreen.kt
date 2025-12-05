package com.mypage.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.shared.R
import com.shared.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QRScanScreen(
    onScanSuccess: (uuid: String, token: String) -> Unit,
    onBack: () -> Unit
) {
    AppTheme {
        val errorQrPermissionRequired = stringResource(R.string.mypage_error_qr_permission_required)
        val errorQrInvalidCode = stringResource(R.string.mypage_error_qr_invalid_code)
        val errorQrDecodeFailed = stringResource(R.string.mypage_error_qr_decode_failed)
        val errorQrCameraInitFailed = stringResource(R.string.mypage_error_qr_camera_init_failed)
        val backText = stringResource(R.string.back)

        val coroutineScope = rememberCoroutineScope()
        var scanError by remember { mutableStateOf<String?>(null) }

        // 카메라 권한 요청
        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) scanError = errorQrPermissionRequired
            }

        LaunchedEffect(Unit) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }

        Box(modifier = Modifier.fillMaxSize()) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val scannerView = CodeScannerView(ctx)
                    val scanner = CodeScanner(ctx, scannerView)

                    scanner.decodeCallback = DecodeCallback { result ->
                        val raw = result.text ?: ""

                        coroutineScope.launch(Dispatchers.Main) {
                            try {
                                val uri = Uri.parse(raw)
                                val uuid = uri.getQueryParameter("uuid") ?: ""
                                val token = uri.getQueryParameter("token") ?: ""

                                if (uuid.isNotBlank() && token.isNotBlank()) {
                                    onScanSuccess(uuid, token)
                                } else {
                                    scanError = errorQrInvalidCode
                                }
                            } catch (e: Exception) {
                                scanError = errorQrDecodeFailed
                            }
                        }
                    }

                    scanner.setErrorCallback { error ->
                        coroutineScope.launch(Dispatchers.Main) {
                            scanError = errorQrCameraInitFailed.format(error.message ?: "")
                        }
                    }

                    scanner.startPreview()
                    scannerView
                }
            )

            Text(
                text = backText,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .clickable { onBack() }
            )

            scanError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}