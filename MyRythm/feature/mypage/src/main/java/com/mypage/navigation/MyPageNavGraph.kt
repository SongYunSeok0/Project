package com.mypage.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

import com.domain.sharedvm.HeartRateVMContract

import com.mypage.ui.EditScreen
import com.mypage.ui.FAQScreenWrapper
import com.mypage.ui.HeartReportScreen
import com.mypage.ui.MediReportScreen
import com.mypage.ui.MyPageScreen
import com.mypage.ui.QRScanScreen
import com.mypage.viewmodel.BLERegisterViewModel

import com.mypage.viewmodel.MyPageViewModel

fun NavGraphBuilder.mypageNavGraph(
    nav: NavController,
    heartVm: HeartRateVMContract,
    userId: Long,
    onLogoutClick: () -> Unit
) {

    // 마이페이지 메인
    composable<MyPageRoute> {
        val vm: MyPageViewModel = hiltViewModel()

        MyPageScreen(
            viewModel = vm,
            onEditClick = { nav.navigate(EditProfileRoute) },
            onHeartClick = { nav.navigate(HeartReportRoute) },
            onFaqClick   = { nav.navigate(FAQRoute) },
            onMediClick  = { nav.navigate(MediReportRoute) },
            onDeviceRegisterClick = { nav.navigate(QRScanRoute) },
            onLogoutClick = onLogoutClick,
            onWithdrawalSuccess = onLogoutClick
        )
    }

    // 프로필 수정
    composable<EditProfileRoute> {
        EditScreen(onDone = { nav.navigateUp() })
    }

    // 심박수 리포트 화면
    composable<HeartReportRoute> {
        HeartReportScreen(vm = heartVm)   // ← ***domain 인터페이스만 사용***
    }

    // FAQ
    composable<FAQRoute> {
        FAQScreenWrapper()
    }

    // 복약 기록
    composable<MediReportRoute> {
        MediReportScreen(userId = userId)
    }

    // QR 스캔 화면
    composable<QRScanRoute> {
        val bleVM: BLERegisterViewModel = hiltViewModel()

        QRScanScreen(
            onBack = { nav.navigateUp() },
            onScanSuccess = { uuid, token ->
                bleVM.setDeviceInfo(uuid, token)
                nav.navigateUp()
            }
        )
    }

}
