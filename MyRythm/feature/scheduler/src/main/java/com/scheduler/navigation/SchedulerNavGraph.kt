package com.scheduler.navigation

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
    userId: String
) {
    composable<SchedulerRoute> {
        SchedulerScreen()
    }

    composable<RegiRoute> { backStackEntry ->
        val r = backStackEntry.toRoute<RegiRoute>()
        RegiScreen(
            userId    = r.userId,
            drugNames = unpackNames(r.drugNamesCsv),
            times     = r.times,
            days      = r.days,
            onCompleted = { nav.popBackStack() }
        )
    }

    composable<OcrRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<OcrRoute>()
        OcrScreen(
            imagePath = route.path,
            onConfirm = { names, times, days ->
                nav.navigate(
                    RegiRoute(
                        userId       = userId,
                        drugNamesCsv = packNames(names),
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
            onOpenOcr  = { path -> nav.navigate(OcrRoute(path)) },
            onOpenRegi = { nav.navigate(RegiRoute(userId = userId)) }
        )
    }
}