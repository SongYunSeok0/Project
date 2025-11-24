package com.healthinsight.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthinsight.ui.HealthInsightScreen
import com.healthinsight.viewmodel.HealthInsightViewModel

@Composable
fun HealthInsightRoute() {
    val vm: HealthInsightViewModel = hiltViewModel()
    val weeklySteps by vm.weeklySteps.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadAll()
    }

    HealthInsightScreen(
        weeklySteps = weeklySteps
    )
}
