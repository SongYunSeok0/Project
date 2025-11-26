package com.scheduler.ocr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraCaptureScreen(
                onCancel = {
                    setResult(RESULT_CANCELED)
                    finish()
                },
                onCaptured = { file ->
                    setResult(
                        RESULT_OK,
                        Intent().putExtra("imagePath", file.absolutePath)
                    )
                    finish()
                }
            )
        }
    }
}

@Composable
private fun CameraCaptureScreen(
    onCancel: () -> Unit,
    onCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exec = remember { ContextCompat.getMainExecutor(context) }

    var hasCamPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCamPerm = granted
        if (!granted) (context as? Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        if (!hasCamPerm) camPermLauncher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()
    }

    LaunchedEffect(hasCamPerm) {
        if (!hasCamPerm) return@LaunchedEffect

        val provider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
    }

    DisposableEffect(Unit) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val rot = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture.targetRotation = rot
            }
        }
        listener.enable()
        onDispose { listener.disable() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    if (!hasCamPerm) {
                        camPermLauncher.launch(Manifest.permission.CAMERA)
                        return@Button
                    }

                    val photo = File(
                        context.cacheDir,
                        "capture_${System.currentTimeMillis()}.jpg"
                    )
                    val opts = ImageCapture.OutputFileOptions.Builder(photo).build()

                    imageCapture.takePicture(
                        opts,
                        exec,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                                onCaptured(photo)
                            }

                            override fun onError(exc: ImageCaptureException) {}
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6AE0D9)
                )
            ) {
                Text("사진 촬영", color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                )
            ) {
                Text("뒤로 가기", color = Color(0xFF6AE0D9))
            }
        }
    }
}
