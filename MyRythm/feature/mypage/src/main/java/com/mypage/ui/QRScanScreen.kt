package com.mypage.ui

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun QRScanScreen(
    onScanSuccess: (uuid: String, token: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var alreadyScanned by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    // 카메라 권한 요청
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) scanError = "카메라 권한이 필요해!"
    }

    LaunchedEffect(Unit) {
        cameraLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // ★ ML Kit 옵션 (이게 가장 안정적)
                    val scannerOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
                        )
                        .build()

                    val scanner = BarcodeScanning.getClient(scannerOptions)

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()

                    analysis.setAnalyzer(
                        ContextCompat.getMainExecutor(ctx)
                    ) { imageProxy ->

                        if (alreadyScanned) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image ?: run {
                            imageProxy.close(); return@setAnalyzer
                        }

                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val raw = barcode.rawValue ?: continue

                                    Log.d("QR", "🔥 RAW = $raw")

                                    val uuid = Regex("uuid=([^&]+)").find(raw)?.groupValues?.get(1) ?: ""
                                    val token = Regex("token=([^&]+)").find(raw)?.groupValues?.get(1) ?: ""

                                    Log.d("QR", "🔥 UUID = $uuid")
                                    Log.d("QR", "🔥 TOKEN = $token")

                                    if (uuid.isNotBlank() && token.isNotBlank()) {
                                        alreadyScanned = true
                                        onScanSuccess(uuid, token)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("QR", "MLKit Error: ${e.message}")
                                scanError = "QR 인식 실패: ${e.message}"
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (e: Exception) {
                        scanError = "카메라 실행 실패: ${e.message}"
                        Log.e("QR", "Camera bind error", e)
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // UI : 뒤로가기 버튼
        Text(
            text = "뒤로가기",
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clickable { onBack() }
        )

        // UI : 에러 텍스트
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
