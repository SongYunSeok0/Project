package com.news.navigation

import kotlinx.serialization.Serializable

@Serializable data object NewsNavGraph
@Serializable data class NewsRoute(val userId: String)

@Serializable data class NewsDetailRoute(val url: String)
