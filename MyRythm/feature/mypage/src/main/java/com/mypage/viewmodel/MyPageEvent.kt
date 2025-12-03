package com.mypage.viewmodel

sealed class MyPageEvent {
    object WithdrawalSuccess : MyPageEvent()
    object WithdrawalFailed : MyPageEvent()
    object LogoutSuccess : MyPageEvent()
    object LogoutFailed : MyPageEvent()
    object LoadFailed : MyPageEvent()
    object InquirySubmitSuccess : MyPageEvent()
    data class InquirySubmitFailed(val message: String) : MyPageEvent()
    data class DeviceRegisterSuccess(val uuid: String) : MyPageEvent()
    data object DeviceRegisterFailed : MyPageEvent()
}