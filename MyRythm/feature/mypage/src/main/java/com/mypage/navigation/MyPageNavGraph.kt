package com.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

import com.mypage.ui.EditScreen
import com.mypage.ui.FAQScreenWrapper
import com.mypage.ui.HeartReportScreen
import com.mypage.ui.MediReportScreen
import com.mypage.ui.MyPageScreen

import com.domain.sharedvm.MainVMContract
import com.domain.sharedvm.HeartRateVMContract
import com.domain.sharedvm.StepVMContract

fun NavGraphBuilder.mypageNavGraph(
    nav: NavController,
    mainVm: MainVMContract,
    heartVm: HeartRateVMContract,
    stepVm: StepVMContract,
    onLogoutClick: () -> Unit
) {
    composable<MyPageRoute> {
        MyPageScreen(
            onEditClick = { nav.navigate(EditProfileRoute) },
            onHeartClick = { nav.navigate(HeartReportRoute) },
            onFaqClick   = { nav.navigate(FAQRoute) },
            onMediClick  = { nav.navigate(MediReportRoute) },
            onLogoutClick = onLogoutClick,
            onWithdrawalSuccess = onLogoutClick
        )
    }

    composable<EditProfileRoute> {
        EditScreen(onDone = { nav.navigateUp() })
    }

    composable<HeartReportRoute> {
        HeartReportScreen(vm = heartVm)
    }

    composable<FAQRoute> {
        FAQScreenWrapper()
    }

    composable<MediReportRoute> {
        MediReportScreen()
    }
}
