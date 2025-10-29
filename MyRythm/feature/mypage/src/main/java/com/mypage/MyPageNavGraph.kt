package com.mypage

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.navigation.*

fun NavGraphBuilder.mypageNavGraph(nav: NavController) {
    composable<MyPageRoute> {
        MyPageScreen(
            onEditClick = { nav.navigate(EditProfileRoute()) },
            onHeartClick = { nav.navigate(HeartReportRoute()) }, // reportId 필요 없으면 Routes에서 기본값 제공
            onLogoutClick = { /* TODO */ }
        )
    }
    composable<EditProfileRoute> { e ->
        val userId = e.toRoute<EditProfileRoute>().userId
        EditScreen(userId = userId, onDone = { nav.navigateUp() })
    }
    composable<HeartReportRoute> { e ->
        HeartReportScreen()
    }
}
