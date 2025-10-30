package com.navigation

import kotlinx.serialization.Serializable

/* 루트 그래프 */
@Serializable data object AuthGraph
@Serializable data object MainGraph

/* Auth */
@Serializable data object LoginRoute
@Serializable data object PwdRoute
@Serializable data object SignupRoute

/* 메인 탭 */
@Serializable data object MainRoute        // 홈
@Serializable data object MapRoute
@Serializable data  object NewsRoute
@Serializable data object SchedulerRoute
@Serializable data object MyPageRoute
@Serializable data object ChatBotRoute

/* 상세/하위 */
@Serializable data class NewsDetailRoute(val url: String)
@Serializable data class EditProfileRoute(val userId: String? = null)
@Serializable data class HeartReportRoute(val reportId: String? = null)
@Serializable data class RegiRoute(val schedId: String? = null)
@Serializable data class OcrRoute(val source: String? = null)
@Serializable data class CameraRoute(val mode: String = "photo")