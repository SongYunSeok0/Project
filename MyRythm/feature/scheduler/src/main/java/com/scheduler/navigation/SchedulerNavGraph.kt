package com.scheduler.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.scheduler.ui.CameraScreen
import com.scheduler.ui.OcrScreen
import com.scheduler.ui.RegiScreen
import com.scheduler.ui.SchedulerScreen

fun NavGraphBuilder.schedulerNavGraph(
    nav: NavHostController,
    fallbackUserId: String = "1"
) {
    // 🟢 일정 목록 화면
    composable<SchedulerRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<SchedulerRoute>()
        val uid = route.userId.ifBlank { fallbackUserId }

        SchedulerScreen(
            userId = uid,
            onOpenRegi = {
                val tempId = System.currentTimeMillis()
                nav.navigate(RegiRoute(userId = uid, prescriptionId = tempId))
            }
        )
    }

    // 🟢 수동 등록 화면
    composable<RegiRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RegiRoute>()

        // 원본 값(디버깅용)
        val rawId = route.userId

        // 비어있으면 fallbackUserId("1")로 대체
        val effectiveId = rawId.ifBlank { fallbackUserId }

        val uidLong = effectiveId.toLongOrNull()
        if (uidLong != null && uidLong > 0L) {
            RegiScreen(
                userId = uidLong,
                prescriptionId = route.prescriptionId,
                onCompleted = { nav.popBackStack() }
            )
        } else {
            Log.e(
                "SchedulerNavGraph",
                "❌ RegiRoute userId 변환 실패: raw='$rawId', effective='$effectiveId'"
            )
        }
    }

    // 🟢 OCR 화면
    composable<OcrRoute> {
        val route = it.toRoute<OcrRoute>()

        // route.userId 는 CameraRoute → OcrRoute 에서 전달됨
        val uid = route.userId

        OcrScreen(
            imagePath = route.path,
            onConfirm = { _, _, _ ->
                val newPrescriptionId = System.currentTimeMillis()
                nav.navigate(
                    RegiRoute(
                        userId = uid,  // ⬅⬅⬅ 여기 반드시!! route.userId 써야 함
                        prescriptionId = newPrescriptionId
                    )
                )
            },
            onRetake = { nav.popBackStack() }
        )
    }


    // 카메라
    composable<CameraRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CameraRoute>()
        val uid = route.userId.ifBlank { fallbackUserId }

        CameraScreen(
            onOpenOcr = { path ->
                // 🔥 반드시 path -> userId 순으로 넣기
                nav.navigate(
                    OcrRoute(
                        path = path,
                        userId = uid
                    )
                )
            },
            onOpenRegi = {
                val tempId = System.currentTimeMillis()
                nav.navigate(RegiRoute(userId = uid, prescriptionId = tempId))
            }
        )
    }
}