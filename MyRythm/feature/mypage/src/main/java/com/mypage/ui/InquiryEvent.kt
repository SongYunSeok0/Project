package com.mypage.ui

sealed interface InquiryEvent {
    data object SubmitSuccess : InquiryEvent
    data class SubmitFailed(val message: String) : InquiryEvent
}
