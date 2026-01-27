package com.domain.usecase.mypage

import com.domain.validation.EditProfileValidationError
import javax.inject.Inject

class ValidateEditProfileUseCase @Inject constructor() {

    operator fun invoke(
        hasRealName: Boolean,
        name: String,
        hasRealPhone: Boolean,
        phone: String,
        hasRealGender: Boolean,
        gender: String,
        hasRealEmail: Boolean,
        email: String,
        isEmailVerified: Boolean,
        isTestGuardian: Boolean,
        protEmail: String,
        isProtEmailVerified: Boolean,
    ): EditProfileValidationError? {

        if (!hasRealName && name.isBlank()) return EditProfileValidationError.EmptyName
        if (!hasRealPhone && phone.isBlank()) return EditProfileValidationError.EmptyPhone
        if (!hasRealGender && gender.isBlank()) return EditProfileValidationError.EmptyGender
        if (!hasRealEmail && email.isBlank()) return EditProfileValidationError.EmptyEmail
        if (!hasRealEmail && !isEmailVerified) return EditProfileValidationError.EmailNotVerified

        // 보호자: 테스트 가디언 아니고, protEmail 입력했는데 인증 안됨이면 막기 (기존 로직 그대로)
        if (!isTestGuardian && protEmail.isNotBlank() && !isProtEmailVerified) {
            return EditProfileValidationError.GuardianNotVerified
        }

        return null
    }
}
