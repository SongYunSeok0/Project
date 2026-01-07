// MyPageUiState.kt
package com.mypage.viewmodel

import com.domain.model.UserProfile
import com.mypage.ui.UiError

data class MyPageUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null
)
