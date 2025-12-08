package com.healthinsight.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.healthinsight.ui.HealthInsightScreen

fun NavGraphBuilder.healthInsightNavGraph() {
    composable<HealthInsightRoute> {
        HealthInsightScreen()
    }
}