package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.model.Plan
import com.domain.model.RegiHistory
import com.domain.sharedvm.MainVMContract
import com.domain.usecase.plan.GetPlansUseCase
import com.domain.usecase.plan.UpdatePlanUseCase
import com.domain.usecase.push.GetFcmTokenUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.domain.usecase.regi.GetRegiHistoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getRegiHistoriesUseCase: GetRegiHistoriesUseCase,
    private val tokenStore: TokenStore,
    private val updatePlanUseCase: UpdatePlanUseCase,
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase
) : ViewModel(), MainVMContract {

    // 다음 복용 시간 ("HH:mm")
    private val _nextTime = MutableStateFlow<String?>(null)
    val nextTime = _nextTime.asStateFlow()

    // 다음 약 라벨
    private val _nextLabel = MutableStateFlow<String?>(null)
    override val nextLabel = _nextLabel.asStateFlow()

    // 남은 시간 ("00:12")
    private val _remainText = MutableStateFlow<String?>(null)
    override val remainText = _remainText.asStateFlow()

    // 다음 복용할 Plan
    private val _nextPlan = MutableStateFlow<Plan?>(null)
    override val nextPlan = _nextPlan.asStateFlow()

    // 미리보기 연장 시간
    private val _previewExtendMinutes = MutableStateFlow(0)
    override val previewExtendMinutes = _previewExtendMinutes.asStateFlow()

    override fun previewExtend(minutes: Int) {
        _previewExtendMinutes.value = minutes
    }

    override fun clearPreview() {
        _previewExtendMinutes.value = 0
    }

    init {
        val userId = JwtUtils.extractUserId(tokenStore.current().access)?.toLongOrNull()
        if (userId != null && userId > 0) {
            load(userId)
        }
        initFcmToken()
    }

    // RegiHistory와 Plan을 모두 보관
    private val _histories = MutableStateFlow<List<RegiHistory>>(emptyList())
    private val _plans = MutableStateFlow<List<Plan>>(emptyList())

    // RegiHistory 먼저 로딩
    private fun load(userId: Long) {
        // RegiHistory 구독
        getRegiHistoriesUseCase()
            .onEach { histories ->
                _histories.value = histories
                updateNextPlan(_plans.value, histories)
            }
            .launchIn(viewModelScope)

        // Plan 구독
        getPlansUseCase(userId)
            .onEach { plans ->
                _plans.value = plans
                updateNextPlan(plans, _histories.value)
            }
            .launchIn(viewModelScope)
    }

    // 약 시간 연장 적용
    override suspend fun extendPlanMinutesSuspend(minutes: Int): Boolean {
        val plan = _nextPlan.value ?: return false
        val oldTime = plan.takenAt ?: return false
        val newTime = oldTime + minutes * 60_000L

        val userId = JwtUtils
            .extractUserId(tokenStore.current().access)
            ?.toLongOrNull()
            ?: return false

        // ✅ 같은 시간대의 모든 Plan 찾기
        val samePlans = _plans.value.filter {
            it.takenAt == oldTime && it.taken == null
        }

        // ✅ 모든 Plan 업데이트
        var allSuccess = true
        samePlans.forEach { p ->
            val updated = p.copy(takenAt = newTime)
            val ok = updatePlanUseCase(userId, updated)
            if (!ok) allSuccess = false
        }

        return allSuccess
    }

    // 약 복용 완료 처리
    override fun finishPlan() {
        val plan = _nextPlan.value ?: return
        val userId = JwtUtils.extractUserId(tokenStore.current().access)?.toLongOrNull() ?: return

        val now = System.currentTimeMillis()
        val updated = plan.copy(taken = now)

        viewModelScope.launch {
            updatePlanUseCase(userId, updated)
            // load는 자동으로 Flow에서 업데이트됨
        }
    }

    // 다음 복용 일정 계산
    private fun updateNextPlan(plans: List<Plan>, histories: List<RegiHistory>) {
        val now = System.currentTimeMillis()

        val next = plans
            .filter {
                it.takenAt != null &&
                        it.takenAt!! >= now &&
                        it.taken == null
            }
            .minByOrNull { it.takenAt!! }

        _nextPlan.value = next

        if (next != null) {
            // ✅ 수정: regihistoryId로 매칭되는 history를 직접 찾기
            val matchedHistory = histories.find { it.id == next.regihistoryId }
            val label = matchedHistory?.label ?: "복용 알림"

            _nextLabel.value = label

            val nextAt = next.takenAt!!
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            _nextTime.value = formatter.format(Date(nextAt))

            val diff = nextAt - now
            val totalMinutes = diff / 1000 / 60
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60

            _remainText.value = String.format("%02d:%02d", hours, mins)
        } else {
            _nextLabel.value = null
            _nextTime.value = null
            _remainText.value = "-:-"
        }
    }

    // FCM 토큰 등록
    private fun initFcmToken() {
        viewModelScope.launch {
            val token = getFcmTokenUseCase()
            if (token != null) {
                runCatching { registerFcmTokenUseCase(token) }
                    .onFailure { Log.w("MainVM", "토큰 등록 실패", it) }
            }
        }
    }
}