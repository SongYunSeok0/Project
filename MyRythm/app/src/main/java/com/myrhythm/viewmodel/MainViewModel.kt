package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.model.Plan
import com.domain.usecase.plan.GetPlansUseCase
import com.domain.usecase.push.GetFcmTokenUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.domain.usecase.plan.UpdatePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPlansUseCase: GetPlansUseCase,
    private val tokenStore: TokenStore,
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    private val updatePlanUseCase: UpdatePlanUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase
) : ViewModel() {

    private val _nextTime = MutableStateFlow<String?>(null)
    val nextTime = _nextTime

    private val _remainText = MutableStateFlow<String?>(null)
    val remainText = _remainText

    private val _nextPlan = MutableStateFlow<Plan?>(null)
    val nextPlan = _nextPlan.asStateFlow()

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

        // 현재 시간 이후의 플랜 중 가장 빠른 것 찾기
        val next = plans
            .filter { it.takenAt != null && it.takenAt!! >= now }
            .minByOrNull { it.takenAt!! }

        // ✅ 찾은 Plan 객체를 StateFlow에 업데이트 (팝업용)
        _nextPlan.value = next

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

    fun updatePlanTime(planId: Long, newHour: Int, newMinute: Int) {
        val currentPlan = _nextPlan.value ?: return
        val currentTakenAt = currentPlan.takenAt ?: return

        // 기존 날짜(년/월/일)는 유지하고, 사용자가 선택한 시간/분만 변경
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTakenAt
        calendar.set(Calendar.HOUR_OF_DAY, newHour)
        calendar.set(Calendar.MINUTE, newMinute)
        calendar.set(Calendar.SECOND, 0)

        val newTimeInMillis = calendar.timeInMillis

        // ⭐ [수정] Plan 객체를 복사해서 시간을 변경
        val updatedPlan = currentPlan.copy(takenAt = newTimeInMillis)

        // ⭐ [수정] UserId 가져오기
        val access = tokenStore.current().access
        val userId = JwtUtils.extractUserId(access)?.toLongOrNull()

        if (userId == null) {
            Log.e("MainViewModel", "UserId가 없어 업데이트 불가")
            return
        }

        viewModelScope.launch {
            // ⭐ [수정] UseCase 호출 (userId와 Plan 객체 전달)
            val success = updatePlanUseCase(userId, updatedPlan)

            if (success) {
                Log.i("MainViewModel", "시간 변경 성공: $newHour:$newMinute")
                // 성공 시, 데이터 갱신 (화면 즉시 반영)
                observePlans(userId)
            } else {
                Log.e("MainViewModel", "시간 변경 실패")
            }
        }
    }
}