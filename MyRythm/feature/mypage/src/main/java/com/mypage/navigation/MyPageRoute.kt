package com.mypage.navigation

import kotlinx.serialization.Serializable

@Serializable data object MyPageNavGraph
@Serializable data class MyPageRoute(val userId: String? = null)
@Serializable data class HeartReportRoute(val userId: String? = null)
@Serializable data class EditProfileRoute(val userId: String? = null)

@Serializable data class FAQRoute(val userId: String? = null)