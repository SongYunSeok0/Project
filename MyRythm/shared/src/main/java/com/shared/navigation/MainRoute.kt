package com.shared.navigation

import kotlinx.serialization.Serializable

@Serializable data object MainNavGraph
@Serializable data class MainRoute(val userId: String)