package com.map.navigation

import kotlinx.serialization.Serializable

@Serializable data object MapNavGraph
@Serializable data object MapRoute
// 상세화면 생기면
// @Serializable data class MapDetailRoute(val placeId: String? = null)