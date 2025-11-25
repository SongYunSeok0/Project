package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.HeartRateHistory
import com.domain.repository.InquiryRepository
import com.domain.usecase.auth.LogoutUseCase
import com.domain.model.UserProfile
import com.domain.repository.AuthRepository
import com.domain.repository.ProfileRepository
import com.domain.usecase.health.GetLatestHeartRateUseCase
import com.domain.usecase.health.GetHeartHistoryUseCase
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
    private val userRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val getLatestHeartRateUseCase: GetLatestHeartRateUseCase,
    private val getHeartHistoryUseCase: GetHeartHistoryUseCase,

    ) : ViewModel() {

    // -------------------------------
    //  이벤트 채널
    // -------------------------------
    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // -------------------------------
    //  프로필 상태
    // -------------------------------
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    // -------------------------------
    //  문의 리스트
    // -------------------------------
    val inquiries = inquiryRepository.getInquiries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------
    //  프로필 불러오기
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
    //  심박수 - 최신 1개
    // -------------------------------
    private val _latestHeartRate = MutableStateFlow<Int?>(null)
    val latestHeartRate: StateFlow<Int?> = _latestHeartRate

    fun loadLatestHeartRate() {
        viewModelScope.launch {
            runCatching {
                getLatestHeartRateUseCase()      // suspend operator fun invoke(): Int?
            }.onSuccess { bpm ->
                _latestHeartRate.value = bpm
            }.onFailure {
                // TODO: 에러 처리 필요하면 이벤트 보내기
            }
        }
    }

    // -------------------------------
    //  심박수 - 최근 측정 기록 리스트
    // -------------------------------
    private val _heartHistory = MutableStateFlow<List<HeartRateHistory>>(emptyList())
    val heartHistory: StateFlow<List<HeartRateHistory>> = _heartHistory

    fun loadHeartHistory() {
        viewModelScope.launch {
            runCatching {
                getHeartHistoryUseCase()
            }.onSuccess { list ->
                _heartHistory.value = list
            }.onFailure {
                // TODO: 로그 찍어도 좋음
            }
        }
    }

    fun refreshHeartData() {
        loadLatestHeartRate()
        loadHeartHistory()
    }

    fun deleteAccount() = viewModelScope.launch {
        runCatching {
            // 👇 userRepository가 아니라 authRepository를 호출해야 합니다!
            authRepository.withdrawal()
        }.onSuccess { isSuccess ->
            if (isSuccess) {
                _events.send(MyPageEvent.WithdrawalSuccess)
            } else {
                _events.send(MyPageEvent.WithdrawalFailed)
            }
        }.onFailure {
            _events.send(MyPageEvent.WithdrawalFailed)
        }
    }

    init {
        loadProfile()
        refreshHeartData()
    }

}
