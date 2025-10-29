package com.navigation

import kotlinx.serialization.Serializable

/* 루트 그래프 */
@Serializable object AuthGraph
@Serializable object MainGraph

/* Auth */
@Serializable object LoginRoute
@Serializable object PwdRoute
@Serializable object SignupRoute

/* 메인 탭 */
@Serializable object MainRoute        // 홈
@Serializable object MapRoute
@Serializable object NewsRoute
@Serializable object SchedulerRoute
@Serializable object MyPageRoute
@Serializable object ChatBotRoute

/* 상세/하위 */
@Serializable data class NewsDetailRoute(val url: String)
@Serializable data class EditProfileRoute(val userId: String? = null)
@Serializable data class HeartReportRoute(val reportId: String? = null)
@Serializable data class RegiRoute(val schedId: String? = null)
@Serializable data class OcrRoute(val source: String? = null)
@Serializable data class CameraRoute(val mode: String = "photo")