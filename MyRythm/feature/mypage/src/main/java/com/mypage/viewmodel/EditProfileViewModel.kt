package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import com.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: ProfileRepository
) : ViewModel() {

    // 서버에서 가져온 프로필 (도메인 모델 그대로)
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    // 저장/로딩 결과 이벤트
    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    fun loadProfile() = viewModelScope.launch {
        runCatching { userRepository.getProfile() }
            .onSuccess { userProfile ->
                _profile.value = userProfile
            }
            .onFailure {
                _events.send(EditProfileEvent.LoadFailed)
            }
    }

    /**
     * UI에서 문자열로 받은 값들을 도메인 UserProfile로 변환해서 저장
     */
    fun saveProfile(
        username: String,
        heightText: String,
        weightText: String,
        ageText: String,   // 1125
        gender: String? = null,
        phone: String?,
        prot_email: String?,
        email: String,
    ) = viewModelScope.launch {

        // 문자열 → 숫자 변환 (실패하면 null)
        val height = heightText.toDoubleOrNull()
        val weight = weightText.toDoubleOrNull()
        val age = ageText.toIntOrNull()

        val newProfile = UserProfile(
            username = username,
            height = height,
            weight = weight,
            //age = age,
            //birth_date = profile.value?.birth_date,
            // 1125         // 🔥 ageText 를 나이(Int)가 아니라 "yyyy-mm-dd" 그대로 birth_date 로 사용
            age = null,                    // 🔥 나이 사용 X (고정)
            birth_date = ageText,          // 🔥 생년월일 저장 — 핵심 수정 부분
            gender = gender,
            phone = phone,
            prot_email = prot_email,
            email = email,
        )

        runCatching {
            userRepository.updateProfile(newProfile)
        }.onSuccess { updatedProfile ->
            _profile.value = updatedProfile
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
