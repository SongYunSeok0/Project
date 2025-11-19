package com.mypage.viewmodel

sealed interface InquiryEvent {
    data object SubmitSuccess : InquiryEvent
    data class SubmitFailed(val message: String) : InquiryEvent
}
