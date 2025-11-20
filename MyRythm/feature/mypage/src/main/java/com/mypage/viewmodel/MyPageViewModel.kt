package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.repository.InquiryRepository
import com.domain.repository.UserRepository   // 🔥 UserProfile 불러오는 repo 필요
import com.domain.usecase.auth.LogoutUseCase
import com.domain.model.UserProfile           // 🔥 프로필 모델
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val inquiryRepository: InquiryRepository,
    private val userRepository: UserRepository           // 🔥 프로필 호출하는 repository
) : ViewModel() {

    // -------------------------------
    //  이벤트 채널
    // -------------------------------
    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // -------------------------------
    //  🔥 프로필 상태
    // -------------------------------
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    // -------------------------------
    //  문의 리스트
    // -------------------------------
    val inquiries = inquiryRepository.getInquiries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------
    //  🔥 프로필 불러오기
    // -------------------------------
    fun loadProfile() = viewModelScope.launch {
        runCatching {
            userRepository.getProfile()     // suspend fun getProfile(): UserProfile
        }.onSuccess { profileData ->
            _profile.value = profileData
        }.onFailure {
            _events.send(MyPageEvent.LoadFailed)
        }
    }

    // -------------------------------
    //  로그아웃
    // -------------------------------
    fun onLogout() = viewModelScope.launch {
        runCatching { logoutUseCase() }
            .onSuccess { _events.send(MyPageEvent.LogoutSuccess) }
            .onFailure { _events.send(MyPageEvent.LogoutFailed) }
    }

    // -------------------------------
    //  문의 등록
    // -------------------------------
    fun addInquiry(type: String, title: String, content: String) {
        viewModelScope.launch {
            runCatching {
                inquiryRepository.addInquiry(type, title, content)
            }.onSuccess {
                _events.send(MyPageEvent.InquirySubmitSuccess)
            }.onFailure { e ->
                _events.send(MyPageEvent.InquirySubmitFailed(e.message ?: "문의 실패"))
            }
        }
    }

    // -------------------------------
    // 🔥 화면 열자마자 프로필 자동 로딩
    // -------------------------------
    init {
        loadProfile()
    }
}
