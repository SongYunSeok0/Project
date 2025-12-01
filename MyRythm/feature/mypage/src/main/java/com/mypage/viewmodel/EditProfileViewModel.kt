package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

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
}
