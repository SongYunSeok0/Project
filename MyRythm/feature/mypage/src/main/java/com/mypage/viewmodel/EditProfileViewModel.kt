package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: ProfileRepository,
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase
) : ViewModel() {

    val profile = userRepository.observeLocalProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // [수정] 본인 이메일 체크 로직 추가
    fun sendEmailCode(email: String, name: String? = null) = viewModelScope.launch {

        // 1. 본인 이메일인지 확인 (현재 로그인된 프로필 정보와 입력한 이메일 비교)
        val myEmail = profile.value?.email
        if (myEmail != null && myEmail == email) {
            _events.send(EditProfileEvent.Error("본인 계정은 보호자로 등록할 수 없습니다."))
            return@launch
        }

        try {
            // .getOrThrow()를 사용하여 Result 내부의 Boolean 값을 꺼내거나 예외를 발생시킵니다.
            val isSuccess = sendEmailCodeUseCase(email, name).getOrThrow()

            // 반환값이 true일 때만 성공 이벤트 발송
            if (isSuccess) {
                _events.send(EditProfileEvent.EmailSent)
            } else {
                _events.send(EditProfileEvent.Error("가입되지 않은 이메일이거나 전송에 실패했습니다."))
            }
        } catch (e: Exception) {
            // getOrThrow()에서 발생한 예외가 여기서 잡힘
            // e.message나 toString()에 404가 포함되어 있는지 확인
            val is404 = e.message?.contains("404") == true || e.toString().contains("404")

            val errorMessage = if (is404) {
                "가입되지 않은 이메일입니다."
            } else {
                "전송 중 오류가 발생했습니다."
            }
            _events.send(EditProfileEvent.Error(errorMessage))
        }
    }

    fun verifyEmailCode(email: String, code: String, onResult: (Boolean) -> Unit) =
        viewModelScope.launch {
            val ok = runCatching { verifyEmailCodeUseCase(email, code) }.getOrDefault(false)
            onResult(ok)
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
        email: String
    ) = viewModelScope.launch {

        val height = heightText.toDoubleOrNull()
        val weight = weightText.toDoubleOrNull()

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
            userRepository.updateProfile(newProfile)
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