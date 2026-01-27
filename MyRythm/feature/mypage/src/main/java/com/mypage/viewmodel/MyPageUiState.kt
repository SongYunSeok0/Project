package com.mypage.viewmodel

import com.domain.model.UserProfile

data class MyPageUiState(
    val profile: UserProfile? = null,
    val latestHeartRate: Int? = null,
)

