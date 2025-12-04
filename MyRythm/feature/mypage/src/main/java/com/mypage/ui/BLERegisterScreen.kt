package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.BLERegisterViewModel

@Composable
fun BLERegisterScreen(
    viewModel: BLERegisterViewModel = hiltViewModel(),
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

        Text("UUID: ${state.deviceUUID}")
        Text("TOKEN: ${state.deviceToken}")

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

        if (state.loading) {
            Text("BLE 기기와 연결 중...", color = MaterialTheme.colorScheme.primary)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.startRegister() }
        ) {
            Text("기기에 Wi-Fi 정보 전송")
        }
    }
}
