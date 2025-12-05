package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.UserProfile
import com.domain.usecase.auth.CheckEmailDuplicateUseCase
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
import com.domain.usecase.mypage.ObserveUserProfileUseCase
import com.domain.usecase.mypage.UpdateUserProfileUseCase
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
    private val checkEmailDuplicateUseCase: CheckEmailDuplicateUseCase
) : ViewModel() {

    val profile = observeUserProfileUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * 이메일 중복 체크
     */
    fun checkEmailDuplicate(email: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        Log.e("EditProfileVM", "========== 이메일 중복 체크 시작 ==========")
        Log.e("EditProfileVM", "Email: $email")

        runCatching {
            Log.e("EditProfileVM", "UseCase 호출 중...")
            val result = checkEmailDuplicateUseCase(email)
            Log.e("EditProfileVM", "UseCase 결과: $result")
            result
        }
            .onSuccess { isDuplicate ->
                Log.e("EditProfileVM", "✅ 성공: isDuplicate = $isDuplicate")
                onResult(isDuplicate)
            }
            .onFailure { e ->
                Log.e("EditProfileVM", "❌ 실패!")
                Log.e("EditProfileVM", "Exception 타입: ${e.javaClass.simpleName}")
                Log.e("EditProfileVM", "메시지: ${e.message}")
                e.printStackTrace()
                onResult(true)  // 실패 시 안전하게 중복으로 간주
            }
    }

    /**
     * 이메일 인증 코드 전송 (보호자 등록용)
     * - 본인 이메일 체크 로직 포함
     */
    fun sendEmailCode(email: String, name: String? = null) = viewModelScope.launch {
        // 1. 본인 이메일인지 확인 (현재 로그인된 프로필 정보와 입력한 이메일 비교)
        val myEmail = profile.value?.email
        if (myEmail != null && myEmail == email) {
            _events.send(EditProfileEvent.Error("본인 계정은 보호자로 등록할 수 없습니다."))
            return@launch
        }

        // 2. 인증 코드 전송
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
            // getOrThrow()에서 발생한 예외 처리
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