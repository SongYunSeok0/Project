package com.mypage.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypage.viewmodel.BLERegisterViewModel
import com.mypage.viewmodel.MyPageEvent
import com.mypage.viewmodel.MyPageViewModel
import com.shared.R
import com.shared.mapper.toMessage

@Composable
fun MyPageRouteScreen(
    onEditClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onMediClick: () -> Unit = {},
    onDeviceRegisterClick: () -> Unit = {},
    onUserManagementClick: () -> Unit = {},
    onInquiriesManagementClick: () -> Unit = {},
    onWithdrawalSuccess: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel(),
    bleViewModel: BLERegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ✅ 상태는 Route에서 collect
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bleState by bleViewModel.state.collectAsStateWithLifecycle()

    val wifiConfigSentMessage = context.getString(R.string.mypage_message_wifi_config_sent)
    val wifiBleConnectedSuccessMessage = context.getString(R.string.mypage_message_wifi_ble_connected_success)
    val withdrawalSuccessMessage = context.getString(R.string.mypage_message_withdrawal_success)
    val withdrawalFailedMessage = context.getString(R.string.mypage_message_withdrawal_failed)

    // ✅ BLE 상태 변화 → 토스트 (Screen에서 제거)
    LaunchedEffect(bleState.bleConnected, bleState.configSent, bleState.uiError) {
        when {
            bleState.uiError != null -> {
                Toast.makeText(
                    context,
                    bleState.uiError!!.toMessage(context),
                    Toast.LENGTH_SHORT
                ).show()
            }
            bleState.configSent -> {
                Toast.makeText(context, wifiConfigSentMessage, Toast.LENGTH_SHORT).show()
            }
            bleState.bleConnected -> {
                Toast.makeText(context, wifiBleConnectedSuccessMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ MyPage 이벤트 수집 (Screen에서 제거)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPageEvent.WithdrawalSuccess -> {
                    Toast.makeText(context, withdrawalSuccessMessage, Toast.LENGTH_SHORT).show()
                    onWithdrawalSuccess()
                }
                is MyPageEvent.WithdrawalFailed -> {
                    Toast.makeText(context, withdrawalFailedMessage, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    // ✅ 순수 UI 호출
    MyPageScreen(
        state = state,
        onDeleteAccount = { viewModel.deleteAccount() },
        onEditClick = onEditClick,
        onHeartClick = onHeartClick,
        onLogoutClick = onLogoutClick,
        onFaqClick = onFaqClick,
        onMediClick = onMediClick,
        onDeviceRegisterClick = onDeviceRegisterClick,
        onUserManagementClick = onUserManagementClick,
        onInquiriesManagementClick = onInquiriesManagementClick
    )
}
