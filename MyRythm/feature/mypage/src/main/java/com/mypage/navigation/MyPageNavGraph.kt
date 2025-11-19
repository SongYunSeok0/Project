package com.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mypage.ui.EditScreen
import com.mypage.ui.FAQScreenWrapper
import com.mypage.ui.HeartReportScreen
import com.mypage.ui.MyPageScreen

fun NavGraphBuilder.mypageNavGraph(
    nav: NavController,
    onLogoutClick: () -> Unit
) {
    composable<MyPageRoute> { backStackEntry ->

        // MyPageRoute 에 userId가 들어옴
        val args = backStackEntry.toRoute<MyPageRoute>()
        val uid = args.userId ?: ""

        MyPageScreen(
            onEditClick = { nav.navigate(EditProfileRoute(uid)) },
            onHeartClick = { nav.navigate(HeartReportRoute(uid)) },
            onFaqClick   = { nav.navigate(FAQRoute(uid)) },
            onLogoutClick = onLogoutClick
        )
    }

    composable<EditProfileRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<EditProfileRoute>()
        EditScreen(onDone = { nav.navigateUp() })
    }

    composable<HeartReportRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<HeartReportRoute>()
        HeartReportScreen(userId = args.userId)
    }

    composable<FAQRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<FAQRoute>()
        FAQScreenWrapper(userId = args.userId)
    }
}

