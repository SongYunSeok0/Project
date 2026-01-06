package com.healthinsight.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.domain.model.DailyStep
import com.domain.usecase.health.DailyHeartRateUI
import com.domain.usecase.plan.MedicationDelayUI
import com.healthinsight.ui.components.HeartRateCard
import com.healthinsight.ui.components.MedicationDelayCard
import com.healthinsight.ui.components.StepsCard
import com.healthinsight.viewmodel.HealthInsightViewModel
import com.shared.ui.theme.AppTheme

@Composable
fun HealthInsightScreen(
    viewModel: HealthInsightViewModel = hiltViewModel()
) {
    AppTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbar = remember { SnackbarHostState() }

        // 에러 메시지 표시
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let {
                snackbar.showSnackbar(it)
                viewModel.clearError()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.loadAll()
        }

        LaunchedEffect(uiState.weeklyHeartRates) {
            println("UI received ${uiState.weeklyHeartRates.size} days of heart rate data")
            uiState.weeklyHeartRates.forEach { day ->
                println("UI: ${day.date} - ${day.measurements.size} measurements")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            HealthInsightContent(
                weeklySteps = uiState.weeklySteps,
                weeklyHeartRates = uiState.weeklyHeartRates,
                medicationDelays = uiState.medicationDelays,
                isLoading = uiState.isLoading,
                onInsertTestData = { viewModel.insertTestData() }
            )

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun HealthInsightContent(
    weeklySteps: List<DailyStep>,
    weeklyHeartRates: List<DailyHeartRateUI>,
    medicationDelays: List<MedicationDelayUI>,
    isLoading: Boolean,
    onInsertTestData: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🔥 개발용 테스트 버튼 (배포 시 제거)
            Button(
                onClick = onInsertTestData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("걸음수 테스트 데이터 삽입")
            }

            if (medicationDelays.isNotEmpty()) {
                MedicationDelayCard(medicationDelays)
            }

            if (weeklyHeartRates.isNotEmpty()) {
                HeartRateCard(weeklyHeartRates)
            }

            if (weeklySteps.isNotEmpty()) {
                StepsCard(weeklySteps)
            }
        }
    }
}