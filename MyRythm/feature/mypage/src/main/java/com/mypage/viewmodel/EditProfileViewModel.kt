package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.UserProfile
import com.domain.usecase.auth.CheckEmailDuplicateUseCase
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
import com.domain.usecase.mypage.ObserveUserProfileUseCase
import com.domain.usecase.mypage.UpdateUserProfileUseCase
import com.domain.usecase.mypage.ValidateEditProfileUseCase
import com.domain.validation.EditProfileValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val checkEmailDuplicateUseCase: CheckEmailDuplicateUseCase,
    private val validateEditProfileUseCase: ValidateEditProfileUseCase
    ) : ViewModel() {

    val profile = observeUserProfileUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * 이메일 중복 체크
     */
    fun checkEmailDuplicate(email: String, onResult: (Boolean) -> Unit) =
        viewModelScope.launch {

            when (val result = checkEmailDuplicateUseCase(email)) {
                is ApiResult.Success -> {
                    onResult(result.data)
                }

                is ApiResult.Failure -> {
                    Log.e("EditProfileVM", "이메일 중복 체크 실패: ${result.error}")
                    onResult(true) // 실패 시 안전하게 중복 처리
                }
            }
        }


    /**
     * 이메일 인증 코드 전송 (보호자 등록용)
     * - 본인 이메일 체크 로직 포함
     */
    fun sendEmailCode(email: String, name: String? = null) =
        viewModelScope.launch {

            val myEmail = profile.value?.email
            if (myEmail != null && myEmail == email) {
                _events.send(EditProfileEvent.Error("본인 계정은 보호자로 등록할 수 없습니다."))
                return@launch
            }

            when (val result = sendEmailCodeUseCase(email, name)) {
                is ApiResult.Success -> {
                    _events.send(EditProfileEvent.EmailSent)
                }

                is ApiResult.Failure -> {
                    _events.send(EditProfileEvent.Error("가입되지 않은 이메일이거나 전송에 실패했습니다."))
                }
            }
        }


    fun verifyEmailCode(
        email: String,
        code: String,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {

        when (verifyEmailCodeUseCase(email, code)) {
            is ApiResult.Success -> onResult(true)
            is ApiResult.Failure -> onResult(false)
        }
    }


    fun saveProfile(
        username: String,
        heightText: String,
        weightText: String,
        ageText: String,
        gender: String?,
        phone: String?,
        prot_email: String?,
        prot_name: String?,
        email: String,
        hasRealName: Boolean,
        hasRealPhone: Boolean,
        hasRealGender: Boolean,
        hasRealEmail: Boolean,
        isEmailVerified: Boolean,
        isProtEmailVerified: Boolean,
    ) = viewModelScope.launch {

        val isTestGuardian = (prot_name == "aaa" && prot_email == "aaa@aaa.com")
        val height = heightText.toDoubleOrNull()
        val weight = weightText.toDoubleOrNull()

        val validationError = validateEditProfileUseCase(
            hasRealName = hasRealName,
            name = username,
            hasRealPhone = hasRealPhone,
            phone = phone.orEmpty(),
            hasRealGender = hasRealGender,
            gender = gender.orEmpty(),
            hasRealEmail = hasRealEmail,
            email = email,
            isEmailVerified = isEmailVerified,
            isTestGuardian = isTestGuardian,
            protEmail = prot_email.orEmpty(),
            isProtEmailVerified = isProtEmailVerified,
        )

        if (validationError != null) {
            val message = when (validationError) {
                EditProfileValidationError.EmptyName -> "이름을 입력해주세요."
                EditProfileValidationError.EmptyPhone -> "전화번호를 입력해주세요."
                EditProfileValidationError.EmptyGender -> "성별을 선택해주세요."
                EditProfileValidationError.EmptyEmail -> "이메일을 입력해주세요."
                EditProfileValidationError.EmailNotVerified -> "이메일 인증이 필요합니다."
                EditProfileValidationError.GuardianNotVerified -> "보호자 이메일 인증이 필요합니다."
            }
            _events.send(EditProfileEvent.Error(message))
            return@launch
        }

        val newProfile = UserProfile(
            username = username,
            height = height,
            weight = weight,
            age = null,
            birth_date = ageText,
            gender = gender,
            phone = phone,
            prot_email = prot_email,
            prot_name = prot_name,
            email = email
        )

        runCatching {
            updateUserProfileUseCase(newProfile)
        }.onSuccess {
            _events.send(EditProfileEvent.SaveSuccess)
        }.onFailure {
            _events.send(EditProfileEvent.SaveFailed)
        }
    }
}

sealed interface EditProfileEvent {
    data object LoadFailed : EditProfileEvent
    data object SaveSuccess : EditProfileEvent
    data object SaveFailed : EditProfileEvent
    data object EmailSent : EditProfileEvent
    data class Error(val message: String) : EditProfileEvent
}