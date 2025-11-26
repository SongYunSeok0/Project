package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.model.Plan
import com.domain.usecase.plan.GetPlansUseCase
import com.domain.usecase.push.GetFcmTokenUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase // ✅ 1. Import 추가
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPlansUseCase: GetPlansUseCase,
    private val tokenStore: TokenStore,
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase // ✅ 2. UseCase 추가 주입
) : ViewModel() {

    private val _nextTime = MutableStateFlow<String?>(null)
    val nextTime = _nextTime

    private val _remainText = MutableStateFlow<String?>(null)
    val remainText = _remainText

    init {
        val access = tokenStore.current().access
        val userIdStr = JwtUtils.extractUserId(access)

        val userId = userIdStr?.toLongOrNull()
        if (userId != null && userId > 0) {
            observePlans(userId)
        }

        // 앱 실행 시 자동으로 토큰 초기화 및 서버 등록 시도
        initFcmToken()
    }

    // ✅ 통합된 FCM 초기화 함수
    fun initFcmToken() {
        viewModelScope.launch {
            // 1. 토큰 가져오기 (로컬 캐시 확인 -> 없으면 Firebase SDK)
            val token = getFcmTokenUseCase()

            if (token != null) {
                Log.i("MainViewModel", "FCM Token initialized: $token")

                // 2. ⭐ 서버에 토큰 등록 (앱 켤 때마다 확실하게 동기화)
                // (네트워크 오류 등으로 실패해도 앱 실행엔 지장 없도록 runCatching 사용)
                runCatching {
                    registerFcmTokenUseCase(token)
                }.onFailure {
                    Log.w("MainViewModel", "토큰 서버 등록 실패 (네트워크 이슈 등)", it)
                }
            } else {
                Log.w("MainViewModel", "Failed to initialize FCM Token")
            }
        }
    }

    private fun observePlans(userId: Long) {
        getPlansUseCase(userId)
            .onEach { plans -> updateNextPlan(plans) }
            .launchIn(viewModelScope)
    }

    private fun updateNextPlan(plans: List<Plan>) {
        val now = System.currentTimeMillis()

        val next = plans
            .filter { it.takenAt != null && it.takenAt!! >= now }
            .minByOrNull { it.takenAt!! }

        if (next != null) {
            val nextAt = next.takenAt!!

            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            _nextTime.value = formatter.format(Date(nextAt))

            val diff = nextAt - now
            val totalMinutes = diff / 1000 / 60

            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            _remainText.value = String.format("%02d:%02d", hours, minutes)
        } else {
            _nextTime.value = null
            _remainText.value = "-:-"
        }
    }
}