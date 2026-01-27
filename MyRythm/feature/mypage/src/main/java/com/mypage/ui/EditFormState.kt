package com.mypage.ui

data class EditFormState(
    val name: String = "",
    val height: String = "",
    val weight: String = "",
    val birthYear: String = "",
    val birthMonth: String = "",
    val birthDay: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val gender: String = "",
    val protEmail: String = "",
    val protName: String = "",
    val email: String = "",

    // 인증 입력값
    val emailCode: String = "",
    val protEmailCode: String = "",
)
