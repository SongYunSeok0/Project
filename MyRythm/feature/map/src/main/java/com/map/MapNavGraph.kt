package com.map

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.navigation.MapRoute

fun NavGraphBuilder.mapNavGraph(navController: NavController) {
    composable<MapRoute> { MapScreen(navController) }
}