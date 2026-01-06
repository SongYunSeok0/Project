package com.mypage.viewmodel

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.UserProfile
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.WithdrawalUseCase
import com.domain.usecase.health.GetLatestHeartRateUseCase
import com.domain.usecase.inquiry.GetInquiriesUseCase
import com.domain.usecase.inquiry.AddInquiryUseCase
import com.domain.usecase.mypage.GetUserProfileUseCase
import com.domain.usecase.mypage.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val withdrawalUseCase: WithdrawalUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val getInquiriesUseCase: GetInquiriesUseCase,
    private val addInquiryUseCase: AddInquiryUseCase,
    @Suppress("unused") // MyPageScreen에서 사용됨
    private val getLatestHeartRateUseCase: GetLatestHeartRateUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    // 🔥 최근 심박수 상태 추가
    @Suppress("unused") // MyPageScreen에서 사용됨
    private val _latestHeartRate = MutableStateFlow<Int?>(null)
    val latestHeartRate: StateFlow<Int?> = _latestHeartRate.asStateFlow()

    val inquiries = getInquiriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        Log.e("MyPageViewModel", "🎬 ========== ViewModel 초기화 시작 ==========")
        loadProfile()
        loadLatestHeartRate() // 🔥 심박수 로드 추가

        viewModelScope.launch {
            Log.e("MyPageViewModel", "👂 observeLocalProfile 시작")
            observeUserProfileUseCase().collect { local ->
                Log.e("MyPageViewModel", "📥 로컬 Profile 수신: $local")
                if (local != null) {
                    _profile.value = local
                    Log.e("MyPageViewModel", "✅ Profile 업데이트 완료")
                } else {
                    Log.e("MyPageViewModel", "⚠️ 로컬 Profile이 null")
                }
            }
        }
    }

    fun loadProfile() = viewModelScope.launch {
        Log.e("MyPageViewModel", "📡 ========== loadProfile() 시작 ==========")
        runCatching { getUserProfileUseCase() }
            .onSuccess {
                Log.e("MyPageViewModel", "✅ Profile API 성공: $it")
                _profile.value = it
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ Profile API 실패: ${it.message}", it)
                _events.send(MyPageEvent.LoadFailed)
            }
    }

    // 🔥 최근 심박수 로드
    private fun loadLatestHeartRate() = viewModelScope.launch {
        Log.e("MyPageViewModel", "💓 ========== loadLatestHeartRate() 시작 ==========")
        runCatching {
            getLatestHeartRateUseCase()
        }.onSuccess { heartRate ->
            Log.e("MyPageViewModel", "✅ 최근 심박수: $heartRate bpm")
            _latestHeartRate.value = heartRate
        }.onFailure { e ->
            Log.e("MyPageViewModel", "❌ 심박수 로드 실패: ${e.message}", e)
            _latestHeartRate.value = null
        }
    }

    fun refreshProfile() = viewModelScope.launch {
        Log.e("MyPageViewModel", "🔄 ========== refreshProfile() 시작 ==========")
        runCatching { getUserProfileUseCase() }
            .onSuccess {
                Log.e("MyPageViewModel", "✅ Profile 새로고침 성공: $it")
                _profile.value = it
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ Profile 새로고침 실패: ${it.message}", it)
            }

        // 🔥 프로필 새로고침 시 심박수도 함께 새로고침
        loadLatestHeartRate()
    }

    private var isLoggingOut = false

    fun logout() = viewModelScope.launch {
        if (isLoggingOut) {
            Log.e("MyPageViewModel", "⚠️ 이미 로그아웃 진행 중")
            return@launch
        }
        isLoggingOut = true

        Log.e("MyPageViewModel", "🚪 ========== 로그아웃 시작 ==========")
        runCatching { logoutUseCase() }
            .onSuccess {
                Log.e("MyPageViewModel", "✅ 로그아웃 성공")
                _events.send(MyPageEvent.LogoutSuccess)
            }
            .onFailure {
                Log.e("MyPageViewModel", "❌ 로그아웃 실패: ${it.message}", it)
                _events.send(MyPageEvent.LogoutFailed)
            }
            .also { isLoggingOut = false }
    }

    fun addInquiry(type: String, title: String, content: String) {
        viewModelScope.launch {
            Log.e("MyPageViewModel", "📝 ========== 문의 등록 시작 ==========")
            Log.e("MyPageViewModel", "type: $type, title: $title")
            runCatching { addInquiryUseCase(type, title, content) }
                .onSuccess {
                    Log.e("MyPageViewModel", "✅ 문의 등록 성공")
                    _events.send(MyPageEvent.InquirySubmitSuccess)
                }
                .onFailure { e ->
                    Log.e("MyPageViewModel", "❌ 문의 등록 실패: ${e.message}", e)
                    _events.send(MyPageEvent.InquirySubmitFailed(e.message ?: "문의 실패"))
                }
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        Log.e("MyPageViewModel", "🗑️ ========== 회원 탈퇴 시작 ==========")

        when (withdrawalUseCase()) {
            is ApiResult.Success -> {
                Log.e("MyPageViewModel", "✅ 회원 탈퇴 성공")

                // SharedPreferences 초기화
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit {
                    clear()
                }
                Log.e("MyPageViewModel", "🧹 SharedPreferences 초기화 완료")

                _events.send(MyPageEvent.WithdrawalSuccess)
            }

            is ApiResult.Failure -> {
                Log.e("MyPageViewModel", "❌ 회원 탈퇴 실패")
                _events.send(MyPageEvent.WithdrawalFailed)
            }
        }
    }

}