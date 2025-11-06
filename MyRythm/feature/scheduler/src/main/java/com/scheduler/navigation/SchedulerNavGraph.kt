package com.scheduler.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.scheduler.ui.*

fun NavGraphBuilder.schedulerNavGraph(nav: NavHostController) {

    composable<SchedulerRoute> { SchedulerScreen() }

    composable<RegiRoute> { backStackEntry ->
        val r = backStackEntry.toRoute<RegiRoute>()
        RegiScreen(
            drugNames = unpackNames(r.drugNamesCsv), // 문자열 → 리스트
            times     = r.times,
            days      = r.days
        )
    }

    composable<OcrRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<OcrRoute>()
        OcrScreen(
            imagePath = route.path,
            onConfirm = { names, times, days ->
                nav.navigate(
                    RegiRoute(
                        drugNamesCsv = packNames(names), // 리스트 → 문자열
                        times        = times,
                        days         = days
                    )
                )
            },
            onRetake = { nav.popBackStack() }
        )
    }

    composable<CameraRoute> {
        CameraScreen(
            onOpenOcr = { path -> nav.navigate(OcrRoute(path)) },
            onOpenRegi = { nav.navigate(RegiRoute()) }
        )
    }
}
