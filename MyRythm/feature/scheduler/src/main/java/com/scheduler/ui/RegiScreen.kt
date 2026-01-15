package com.scheduler.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shared.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduler.viewmodel.RegiViewModel
import com.scheduler.ui.components.RegiHeaderSection
import com.scheduler.ui.components.RegiMedicineListSection
import com.scheduler.ui.components.RegiDateTimeSection
import com.scheduler.ui.components.RegiFooterSection
import com.shared.ui.components.AppButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegiScreen(
    modifier: Modifier = Modifier,
    drugNames: List<String> = emptyList(),
    times: Int? = null,
    days: Int? = null,
    viewModel: RegiViewModel = hiltViewModel(),
    onCompleted: () -> Unit = {},
    regihistoryId: Long? = null,
) {
    val registrationSuccessMessage = stringResource(R.string.scheduler_message_registration_success)
    val registrationFailedMessage = stringResource(R.string.scheduler_message_registration_failed)

    val context = LocalContext.current

    // 0115 RegiController.kt 생성
    val controller = rememberRegiController(
        drugNames = drugNames,
        times = times,
        days = days,
        regihistoryId = regihistoryId,
        viewModel = viewModel
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            when (msg) {
                "등록 완료" -> {
                    Toast.makeText(context, registrationSuccessMessage, Toast.LENGTH_SHORT).show()
                    onCompleted()
                }

                "등록 실패" ->
                    Toast.makeText(context, registrationFailedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(regihistoryId) {
        viewModel.initRegi(regihistoryId)
    }

    // IoT 기기 로드
    LaunchedEffect(Unit) {
        viewModel.loadMyDevices()
    }

    // ViewModel에서 도메인 Device 리스트 받기
    // 0115 var selectedDevice 코드는 사용자 드롭다운 입력 상태 값이라 RegiController.kt로 이동
    val devices by viewModel.devices.collectAsState()

    // 초기 데이터 설정 (컨트롤러로 위임)
    LaunchedEffect(Unit) {
        controller.init()
    }

    // 탭 변경 시 로직 (컨트롤러로 위임)
    LaunchedEffect(controller.tab) {
        controller.handleTabChange()
    }

    // 복용 횟수 변경 시 로직 (컨트롤러로 위임)
    LaunchedEffect(controller.dose) {
        controller.syncIntakeTimes()
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RegiHeaderSection(controller = controller)
            RegiMedicineListSection(controller = controller)
            RegiDateTimeSection(controller = controller)
            RegiFooterSection(
                controller = controller,
                devices = devices
            )
            Spacer(Modifier.height(20.dp))
        }
    }
    // 날짜 선택 다이얼로그 헬퍼 함수
    if (controller.showStart) {
        DateSelectionDialog(
            currentDay = controller.startDay,
            onDismiss = { controller.showStart = false },
            onConfirm = { controller.startDay = it }
        )
    }
    if (controller.showEnd) {
        DateSelectionDialog(
            currentDay = controller.endDay,
            onDismiss = { controller.showEnd = false },
            onConfirm = { controller.endDay = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectionDialog(
    currentDay: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val initialMillis = runCatching { dateFmt.parse(currentDay)?.time }.getOrNull()
        ?: System.currentTimeMillis()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    val cancelText = stringResource(R.string.cancel)
    val confirmText = stringResource(R.string.confirm)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppButton(
                text = confirmText,
                height = 40.dp,
                width = 70.dp,
                onClick = {
                    state.selectedDateMillis?.let {
                        onConfirm(dateFmt.format(Date(it)))
                    }
                }
            )
        },
        dismissButton = {
            AppButton(
                text = cancelText,
                height = 40.dp,
                width = 70.dp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = onDismiss
            )
        }
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DatePicker(state = state)
        }
    }
}