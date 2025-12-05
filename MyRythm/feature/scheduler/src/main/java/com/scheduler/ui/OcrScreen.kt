package com.scheduler.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.scheduler.ocr.OcrCropView
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppFieldHeight

@Composable
fun OcrScreen(
    imagePath: String,
    modifier: Modifier = Modifier,
    onConfirm: (names: List<String>, times: Int?, days: Int?) -> Unit,
    onRetake: () -> Unit,
) {
    val photoRecaptureButtonText = stringResource(R.string.photo_recapture_button)
    val confirmText = stringResource(R.string.confirm)
    val guideConfirmRecognitionAreaMessage = stringResource(R.string.scheduler_message_guide_confirm_recognition_area)
    val errorRecognitionNoResultTitle = stringResource(R.string.scheduler_error_recognition_no_result_title)
    val errorRecognitionNoTextDetail = stringResource(R.string.scheduler_error_recognition_no_text_detail)

    var viewRef by remember { mutableStateOf<OcrCropView?>(null) }
    var showNoResult by remember { mutableStateOf(false) }

    if (showNoResult) {
        AlertDialog(
            onDismissRequest = { showNoResult = false },
            confirmButton = {
                AppButton(
                    text = confirmText,
                    height = 40.dp,
                    width = 70.dp,
                    onClick = { showNoResult = false }
                )
            },
            title = { Text(errorRecognitionNoResultTitle,) },
            text  = {
                Text(
                    errorRecognitionNoTextDetail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), modifier = modifier) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        OcrCropView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }.also { viewRef = it }
                    },
                    update = { v -> v.bindImage(imagePath) }
                )
            }

            // 확인 버튼
            AppButton(
                text = guideConfirmRecognitionAreaMessage,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppFieldHeight),
                onClick = {
                    viewRef?.setOnOcrParsed { list ->
                        if (list.isEmpty()) {
                            showNoResult = true
                        } else {
                            val names = list.map { it.first }
                            val maxTimes = list.mapNotNull { it.second }.maxOrNull()?.coerceIn(1, 6)
                            val maxDays = list.mapNotNull { it.third }.maxOrNull()?.coerceAtLeast(1)
                            onConfirm(names, maxTimes, maxDays)
                        }
                    }
                    viewRef?.runOcr {}
                }
            )

            // 다시 촬영 버튼
            AppButton(
                text = photoRecaptureButtonText,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppFieldHeight),
                isOutlined = true,
                onClick = onRetake
            )
        }
    }
}