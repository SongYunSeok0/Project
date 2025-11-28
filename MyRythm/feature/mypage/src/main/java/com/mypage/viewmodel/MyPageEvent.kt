package com.mypage.viewmodel

sealed interface MyPageEvent {
    object LogoutSuccess : MyPageEvent
    object LogoutFailed : MyPageEvent

    object InquirySubmitSuccess : MyPageEvent
    data class InquirySubmitFailed(val message: String) : MyPageEvent

    data object LoadFailed : MyPageEvent

    data object WithdrawalSuccess : MyPageEvent

    data object WithdrawalFailed : MyPageEvent

    data class DeviceRegisterSuccess(val uuid: String) : MyPageEvent
    data object DeviceRegisterFailed : MyPageEvent

}