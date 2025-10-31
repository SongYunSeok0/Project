package com.news.navigation

import kotlinx.serialization.Serializable

@Serializable data object NewsNavGraph
@Serializable data object NewsRoute
@Serializable data class NewsDetailRoute(val url: String)
