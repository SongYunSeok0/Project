package com.domain.validation

sealed class EditProfileValidationError {
    data object EmptyName : EditProfileValidationError()
    data object EmptyPhone : EditProfileValidationError()
    data object EmptyGender : EditProfileValidationError()
    data object EmptyEmail : EditProfileValidationError()
    data object EmailNotVerified : EditProfileValidationError()
    data object GuardianNotVerified : EditProfileValidationError()
}
