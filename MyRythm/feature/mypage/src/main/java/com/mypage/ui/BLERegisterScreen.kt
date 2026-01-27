package com.mypage.ui

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.mapper.toMessage
import com.shared.mapper.toUiError
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import com.mypage.viewmodel.BLERegisterViewModel
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField

@Composable
fun BLERegisterScreen(
    viewModel: BLERegisterViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    // 🔥 BLE 권한 목록
    val blePermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // 🔥 런타임 권한 요청 런처
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.values.all { it }
            if (!allGranted) {
                Toast.makeText(context, "BLE 권한이 필요해요!", Toast.LENGTH_SHORT).show()
            }
        }

    // 🔥 화면 처음 들어오면 권한 요청
    LaunchedEffect(Unit) {
        launcher.launch(blePermissions)
    }

    // 🔥 실제 UI는 따로 함수로 분리
    BLERegisterScreenUI(viewModel, onFinish)
}

@Composable
private fun BLERegisterScreenUI(
    viewModel: BLERegisterViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val ssidText = stringResource(R.string.wifi_ssid)
    val wifiPasswordText = stringResource(R.string.wifi_password)
    val bleConnectingText = stringResource(R.string.ble_wifi_connecting)
    val deviceWifiText = stringResource(R.string.device_wifi_title)
    val deviceNameText = stringResource(R.string.device_name)
    val devicerRgisterText = stringResource(R.string.device_register_button)
    val deviceResetText = stringResource(R.string.device_reset)
    val uuidText = stringResource(R.string.device_uuid)
    val tokenText = stringResource(R.string.device_token)
    val wifiConfigSentMessage = stringResource(R.string.mypage_message_wifi_config_sent)

    // BLE 연결/전송 완료 시 페이지 종료
    LaunchedEffect(state.configSent) {
        if (state.configSent) {
            Toast.makeText(context, wifiConfigSentMessage, Toast.LENGTH_SHORT).show()
            onFinish()
        }
    }

    // 에러 처리
    LaunchedEffect(state.uiError) {
        state.uiError?.let { error ->
            Toast.makeText(
                context,
                error.toMessage(context),  // UiError를 String으로 변환
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            deviceWifiText,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text("$uuidText ${state.deviceUUID}", color = MaterialTheme.colorScheme.onSurface)
        Text("$tokenText ${state.deviceToken}", color = MaterialTheme.colorScheme.onSurface)

        AppInputField(
            value = state.ssid,
            onValueChange = viewModel::updateSSID,
            label = ssidText,
            outlined = true,
            singleLine = true
        )

        AppInputField(
            value = state.pw,
            onValueChange = viewModel::updatePW,
            label = wifiPasswordText,
            outlined = true,
            singleLine = true
        )

        AppInputField(
            value = state.deviceName,
            onValueChange = viewModel::updateDeviceName,
            label = deviceNameText,
            outlined = true,
            singleLine = true
        )

        if (state.loading) {
            Text(
                bleConnectingText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        AppButton(
            text = devicerRgisterText,
            height = 48.dp,
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.startRegister() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppButton(
            text = deviceResetText,
            height = 48.dp,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurface,
            onClick = { viewModel.resetFields() }
        )
    }
}
