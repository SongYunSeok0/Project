package com.mypage.navigation

import kotlinx.serialization.Serializable

@Serializable data object MyPageNavGraph
@Serializable data object MyPageRoute
@Serializable data object HeartReportRoute
@Serializable data class EditProfileRoute(val userId: String? = null)

@Serializable data object FAQRoute