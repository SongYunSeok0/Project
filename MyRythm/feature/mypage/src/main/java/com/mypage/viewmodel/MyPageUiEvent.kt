package com.mypage.viewmodel

sealed interface MyPageUiEvent {
    data object Refresh : MyPageUiEvent
    data object ConfirmWithdrawal : MyPageUiEvent
    data class SubmitInquiry(val type: String, val title: String, val content: String) : MyPageUiEvent
}

