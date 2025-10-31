package com.map.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.map.ui.MapScreen

fun NavGraphBuilder.mapNavGraph() {
    composable<MapRoute> { MapScreen() }
}