package com.scheduler.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image // ✅ 이 Image만 사용
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import com.scheduler.ocr.CameraActivity
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.authTheme
import java.io.File
import java.io.FileOutputStream


@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onOpenOcr: (String) -> Unit = {},
    onOpenRegi: () -> Unit = {},
) {
    AppTheme {
        val photoGuideText = stringResource(R.string.photo_guide)
        val photoCaptureButtonText = stringResource(R.string.photo_capture_button)
        val photoSelectFromGallery = stringResource(R.string.photo_select_from_gallery)
        val manualEntry = stringResource(R.string.manual_entry)
        val guideCaptureBrightAreaMessage = stringResource(R.string.scheduler_message_guide_capture_bright_area)
        val guideCaptureCheckClarityMessage = stringResource(R.string.scheduler_message_guide_capture_check_clarity)

        val context = LocalContext.current

        val takePhotoLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val path = result.data?.getStringExtra("imagePath")
                    ?: return@rememberLauncherForActivityResult
                onOpenOcr(path)
            }
        }

        val galleryPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult
            val path = copyUriToCache(context, uri) ?: return@rememberLauncherForActivityResult
            onOpenOcr(path)
        }

        Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(5.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.authTheme.authBackground.copy(0.2f))
                        .padding(20.dp)
                ) {
                    Text(
                        photoGuideText,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.example),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        guideCaptureBrightAreaMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        guideCaptureCheckClarityMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(10.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppButton(
                        text = photoCaptureButtonText,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppFieldHeight),
                        onClick = {
                            takePhotoLauncher.launch(Intent(context, CameraActivity::class.java))
                        },
                        content = {
                            Image(
                                painter = painterResource(R.drawable.camera),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                    )
                    // 갤러리
                    AppButton(
                        text = photoSelectFromGallery,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppFieldHeight),
                        isOutlined = true,
                        onClick = { galleryPicker.launch("image/*") },
                        content = {
                            Image(
                                painter = painterResource(R.drawable.upload),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                    AppButton(
                        text = manualEntry,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppFieldHeight),
                        onClick = onOpenRegi,
                        content = {
                            Image(
                                painter = painterResource(R.drawable.camera),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun copyUriToCache(context: Context, uri: Uri): String? = try {
    val name = "pick_${System.currentTimeMillis()}.jpg"
    val outFile = File(context.cacheDir, name)
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(outFile).use { output -> input.copyTo(output) }
    }
    outFile.absolutePath
} catch (_: Exception) { null }
