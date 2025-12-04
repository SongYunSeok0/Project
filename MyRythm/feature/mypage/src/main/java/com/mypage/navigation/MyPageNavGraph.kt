package com.mypage.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

import com.domain.sharedvm.HeartRateVMContract
import com.mypage.ui.BLERegisterScreen

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

    // -------------------- MyPage 메인 --------------------
    composable<MyPageRoute> { backStackEntry ->
        val vm: MyPageViewModel = hiltViewModel(backStackEntry)

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


    // -------------------- QR 스캔 화면 --------------------
    composable<QRScanRoute> { backStackEntry ->

        // ⭐ MyPageRoute 범위의 ViewModel 공유
        val parentEntry = nav.getBackStackEntry(MyPageRoute::class)
        val sharedBLEVM: BLERegisterViewModel = hiltViewModel(parentEntry)

        QRScanScreen(
            onBack = { nav.navigateUp() },
            onScanSuccess = { uuid, token ->
                sharedBLEVM.setDeviceInfo(uuid, token)
                nav.navigate(BLERegisterRoute)
            }
        )
    }


    // -------------------- BLE Wi-Fi 설정 화면 --------------------
    composable<BLERegisterRoute> { backStackEntry ->

        val parentEntry = nav.getBackStackEntry(MyPageRoute::class)
        val sharedBLEVM: BLERegisterViewModel = hiltViewModel(parentEntry)

        BLERegisterScreen(
            viewModel = sharedBLEVM,
            onFinish = {
                nav.popBackStack(MyPageRoute, false)
            }
        )
    }


    // -------------------- 프로필 수정 --------------------
    composable<EditProfileRoute> {
        EditScreen(onDone = { nav.navigateUp() })
    }

    // -------------------- 심박 리포트 --------------------
    composable<HeartReportRoute> {
        HeartReportScreen(vm = heartVm)
    }

    // -------------------- FAQ --------------------
    composable<FAQRoute> {
        FAQScreenWrapper()
    }

    // -------------------- 복약 기록 --------------------
    composable<MediReportRoute> {
        MediReportScreen(userId = userId)
    }
}


