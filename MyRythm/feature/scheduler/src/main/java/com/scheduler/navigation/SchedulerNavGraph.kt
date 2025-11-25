package com.scheduler.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.domain.model.RegiHistory
import com.scheduler.ui.CameraScreen
import com.scheduler.ui.OcrScreen
import com.scheduler.ui.RegiScreen
import com.scheduler.ui.SchedulerScreen
import com.shared.navigation.MainRoute


fun NavGraphBuilder.schedulerNavGraph(
    nav: NavHostController,
    fallbackUserId: String = "1"
) {
    // 🟢 일정 목록 화면
    composable<SchedulerRoute> {
        val route = it.toRoute<SchedulerRoute>()
        val uid = route.userId

        SchedulerScreen(userId = uid.toLong())
    }

    composable<RegiRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RegiRoute>()
        val uid = route.userId

        RegiScreen(
            userId = uid.toLong(),
            regiHistoryId = route.regiHistoryId,
            onCompleted = {
                nav.navigate(SchedulerRoute(uid)) {
                    popUpTo(MainRoute(uid)) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }

    // 🟢 OCR 화면
    composable<OcrRoute> {
        val route = it.toRoute<OcrRoute>()

        // route.userId 는 CameraRoute → OcrRoute 에서 전달됨
        val uid = route.userId

        OcrScreen(
            imagePath = route.path,
            onConfirm = { _, _, _ ->
                val newregiHistoryId = System.currentTimeMillis()
                nav.navigate(
                    RegiRoute(
                        userId = uid,  // ⬅⬅⬅ 여기 반드시!! route.userId 써야 함
                        regiHistoryId = newregiHistoryId
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
                nav.navigate(RegiRoute(userId = uid, regiHistoryId = tempId))
            }
        )
    }
}