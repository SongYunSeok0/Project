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
import com.shared.navigation.MainRoute

fun NavGraphBuilder.schedulerNavGraph(nav: NavHostController) {

    // 스케줄러
    composable<SchedulerRoute> {
        val route = it.toRoute<SchedulerRoute>()
        val uid = route.userId
        Log.e("SchedulerRoute", "uid = $uid")

        SchedulerScreen(userId = uid.toLong())
    }

    composable<RegiRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RegiRoute>()
        val uid = route.userId
        Log.e("RegiRoute", "uid = $uid")

        RegiScreen(
            drugNames = route.drugNames,
            times = route.times,
            days = route.days,
            regihistoryId = route.regihistoryId,
            onCompleted = {
                nav.navigate(SchedulerRoute(uid)) {
                    popUpTo(MainRoute(uid)) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }

    composable<OcrRoute> {
        val route = it.toRoute<OcrRoute>()
        val uid = route.userId
        Log.e("OcrRoute", "uid = $uid")

        OcrScreen(
            imagePath = route.path,
            onConfirm = { names, times, days ->
                nav.navigate(
                    RegiRoute(
                        userId = uid,
                        drugNames = names,
                        times = times,
                        days = days,
                        regihistoryId = null
                    )
                )
            },
            onRetake = { nav.popBackStack() }
        )
    }

    // 카메라 화면
    composable<CameraRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CameraRoute>()
        val uid = route.userId
        Log.e("CameraRoute", "uid = $uid")

        CameraScreen(
            onOpenOcr = { path ->
                nav.navigate(OcrRoute(path = path, userId = uid))
            },
            onOpenRegi = {
                nav.navigate(RegiRoute(userId = uid, regihistoryId = null))
            }
        )
    }
}