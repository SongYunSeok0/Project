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
    onLogoutClick: () -> Unit
) {
    composable<MyPageRoute> { backStackEntry ->
        MyPageScreen(
            onEditClick = { nav.navigate(EditProfileRoute) },
            onHeartClick = { nav.navigate(HeartReportRoute) },
            onFaqClick   = { nav.navigate(FAQRoute) },
            onMediClick = { nav.navigate(MediReportRoute) },
            onLogoutClick = onLogoutClick,
            onWithdrawalSuccess = onLogoutClick
        )
    }

    composable<EditProfileRoute> { backStackEntry ->
        EditScreen(onDone = { nav.navigateUp() })
    }

    composable<HeartReportRoute> { backStackEntry ->
        HeartReportScreen()
    }

    composable<FAQRoute> { backStackEntry ->
        FAQScreenWrapper()
    }

    composable<MediReportRoute> {
        MediReportScreen()
    }

}

