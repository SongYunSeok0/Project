package com.map

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.navigation.MapRoute

fun NavGraphBuilder.mapNavGraph() {
    composable<MapRoute> { MapScreen() }
}