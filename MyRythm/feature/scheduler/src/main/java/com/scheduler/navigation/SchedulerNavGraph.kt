package com.scheduler.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.scheduler.ui.SchedulerScreen
import com.scheduler.ui.RegiScreen
import com.scheduler.ui.OcrScreen
import com.scheduler.ui.CameraScreen
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
fun NavGraphBuilder.schedulerNavGraph(nav: NavHostController) {

    // 스케줄 메인
    composable<SchedulerRoute> {
        SchedulerScreen()
    }

    // 일정 등록
    composable<RegiRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RegiRoute>()
        RegiScreen()
    }

    // OCR 스캔
    composable<OcrRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<OcrRoute>()
        OcrScreen()
    }

    // 카메라
    composable<CameraRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CameraRoute>()
        CameraScreen()
    }
}
