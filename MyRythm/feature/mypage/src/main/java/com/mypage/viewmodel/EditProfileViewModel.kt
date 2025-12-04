package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.UserProfile
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
import com.domain.usecase.auth.CheckEmailDuplicateUseCase
import com.domain.usecase.mypage.ObserveUserProfileUseCase  // 👈 추가
import com.domain.usecase.mypage.UpdateUserProfileUseCase  // 👈 추가
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,  // 👈 UseCase로 변경
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,    // 👈 UseCase로 변경
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val checkEmailDuplicateUseCase: CheckEmailDuplicateUseCase
) : ViewModel() {

    val profile = observeUserProfileUseCase()  // 👈 UseCase 사용
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

    fun sendEmailCode(email: String) = viewModelScope.launch {
        runCatching {
            sendEmailCodeUseCase(email)
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
}