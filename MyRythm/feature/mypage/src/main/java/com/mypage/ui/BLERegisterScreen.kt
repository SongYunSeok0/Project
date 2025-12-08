package com.mypage.ui

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.mypage.viewmodel.BLERegisterViewModel

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

    // BLE 연결/전송 완료 시 페이지 종료
    LaunchedEffect(state.configSent) {
        if (state.configSent) {
            Toast.makeText(context, "Wi-Fi 정보 전송 완료!", Toast.LENGTH_SHORT).show()
            onFinish()
        }
    }

    // 에러 처리
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("기기 Wi-Fi 설정", style = MaterialTheme.typography.headlineSmall, color = Color.Black)

        Text("UUID: ${state.deviceUUID}", color = Color.Black)
        Text("TOKEN: ${state.deviceToken}", color = Color.Black)

        OutlinedTextField(
            value = state.ssid,
            onValueChange = viewModel::updateSSID,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Wi-Fi SSID") }
        )

        OutlinedTextField(
            value = state.pw,
            onValueChange = viewModel::updatePW,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Wi-Fi PASSWORD") }
        )

        OutlinedTextField(
            value = state.deviceName,
            onValueChange = viewModel::updateDeviceName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("기기 별명(예: 약통1)") }
        )


        if (state.loading) {
            Text("BLE 기기와 연결 중...", color = MaterialTheme.colorScheme.primary)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.startRegister() }
        ) {
            Text("디바이스 등록 하기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            onClick = { viewModel.resetFields() }
        ) {
            Text("정보 초기화")
        }
    }
}
