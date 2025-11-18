package com.mypage.ui

sealed interface MyPageEvent {
    object LogoutSuccess : MyPageEvent
    object LogoutFailed : MyPageEvent

    object InquirySubmitSuccess : MyPageEvent
    data class InquirySubmitFailed(val message: String) : MyPageEvent
}