package com.scheduler

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.navigation.*

// 현재 화면 시그니처 기준:
// - SchedulerScreen()                // 콜백 없음
// - CameraScreen(onTakePhoto, onPickFromGallery)
// - OcrScreen(onConfirm, onRetake)
// - RegiScreen(onSubmit)

fun NavGraphBuilder.schedulerNavGraph(nav: NavController) {
    composable<SchedulerRoute> {
        // 스케줄 메인
        SchedulerScreen()
    }

    composable<CameraRoute> {
        // 촬영 → OCR 로 이동
        CameraScreen(
            onTakePhoto = { nav.navigate(OcrRoute()) },
            onPickFromGallery = { _ -> nav.navigate(OcrRoute()) }
        )
    }

    composable<OcrRoute> {
        // 확인 → 등록 화면, 다시촬영 → 뒤로
        OcrScreen(
            onConfirm = { nav.navigate(RegiRoute()) },
            onRetake = { nav.navigateUp() }
        )
    }

    composable<RegiRoute> {
        // 등록 완료 → 뒤로
        RegiScreen(
            onSubmit = { nav.navigateUp() }
        )
    }
}
