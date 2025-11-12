package com.scheduler.ui

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.scheduler.ocr.OcrCropView

@Composable
fun OcrScreen(
    imagePath: String,
    modifier: Modifier = Modifier,
    onConfirm: (names: List<String>, times: Int?, days: Int?) -> Unit,
    onRetake: () -> Unit,
) {
    val mint = Color(0xFF6AE0D9)
    var viewRef by remember { mutableStateOf<OcrCropView?>(null) }
    var showNoResult by remember { mutableStateOf(false) }

    if (showNoResult) {
        AlertDialog(
            onDismissRequest = { showNoResult = false },
            confirmButton = { TextButton(onClick = { showNoResult = false }) { Text("확인") } },
            title = { Text("영역 인식 결과 없음") },
            text  = { Text("선택한 영역에서 텍스트를 찾지 못했습니다. 영역을 다시 지정하세요.") }
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F4F6))
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
            Button(
                onClick = {
                    viewRef?.setOnOcrParsed { list ->
                        if (list.isEmpty()) {
                            showNoResult = true
                        } else {
                            val names    = list.map { it.first }
                            val maxTimes = list.mapNotNull { it.second }.maxOrNull()?.coerceIn(1, 6)
                            val maxDays  = list.mapNotNull { it.third  }.maxOrNull()?.coerceAtLeast(1)
                            onConfirm(names, maxTimes, maxDays)
                        }
                    }
                    viewRef?.runOcr { }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mint,
                    contentColor = Color.White
                )
            ) { Text("영역 인식 후 확인") }

            // 다시 촬영 버튼
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, mint),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = mint
                )
            ) { Text("다시 촬영") }
        }
    }
}
