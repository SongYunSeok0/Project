package com.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mypage.ui.EditScreen
import com.mypage.ui.FAQScreenWrapper
import com.mypage.ui.HeartReportScreen
import com.mypage.ui.MediReportScreen
import com.mypage.ui.MyPageScreen

fun NavGraphBuilder.mypageNavGraph(
    nav: NavController,
    onLogoutClick: () -> Unit      // ← 상위에서 주입
) {
    composable<MyPageRoute> {
        MyPageScreen(
            onEditClick = { nav.navigate(EditProfileRoute()) },
            onHeartClick = { nav.navigate(HeartReportRoute) },
            onFaqClick = { nav.navigate(FAQRoute) },
            onMediClick = { nav.navigate(MediReportRoute) },
            onLogoutClick = onLogoutClick       // ← 그대로 전달
        )
    }

    composable<EditProfileRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<EditProfileRoute>()
        EditScreen(userId = args.userId, onDone = { nav.navigateUp() })
    }

    composable<HeartReportRoute> {
        HeartReportScreen()
    }

    composable<FAQRoute> {
        FAQScreenWrapper()
    }

    composable<MediReportRoute> {
        MediReportScreen()
    }
}
