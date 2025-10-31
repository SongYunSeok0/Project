package com.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mypage.ui.EditScreen
import com.mypage.ui.HeartReportScreen
import com.mypage.ui.MyPageScreen

fun NavGraphBuilder.mypageNavGraph(nav: NavController) {
    composable<MyPageRoute> {
        MyPageScreen(
            onEditClick = { nav.navigate(EditProfileRoute()) },
            onHeartClick = { nav.navigate(HeartReportRoute) },
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
