package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.model.Plan
import com.domain.model.RegiHistory
import com.domain.repository.PlanRepository
import com.domain.repository.RegiRepository
import com.domain.sharedvm.MainVMContract
import com.domain.usecase.plan.GetPlanUseCase
import com.domain.usecase.plan.UpdatePlanUseCase
import com.domain.usecase.push.GetFcmTokenUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.domain.usecase.regi.GetRegiHistoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPlansUseCase: GetPlanUseCase,
    private val getRegiHistoriesUseCase: GetRegiHistoriesUseCase,
    private val tokenStore: TokenStore,
    private val updatePlanUseCase: UpdatePlanUseCase,
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val regiRepo: RegiRepository,
    private val planRepo: PlanRepository,
) : ViewModel(), MainVMContract {

    private val _nextTime = MutableStateFlow<String?>(null)
    val nextTime = _nextTime.asStateFlow()

    private val _nextLabel = MutableStateFlow<String?>(null)
    override val nextLabel = _nextLabel.asStateFlow()

    private val _remainText = MutableStateFlow<String?>(null)
    override val remainText = _remainText.asStateFlow()

    private val _nextPlan = MutableStateFlow<Plan?>(null)
    override val nextPlan = _nextPlan.asStateFlow()

    private val _histories = MutableStateFlow<List<RegiHistory>>(emptyList())
    private val _plans = MutableStateFlow<List<Plan>>(emptyList())

    private val _previewExtendMinutes = MutableStateFlow(0)
    override val previewExtendMinutes = _previewExtendMinutes.asStateFlow()

    override fun previewExtend(minutes: Int) {
        _previewExtendMinutes.value = minutes
    }

    override fun clearPreview() {
        _previewExtendMinutes.value = 0
    }

    init {
        try {
            Log.d("MainVM", "init 시작")
            val userId = JwtUtils.extractUserId(tokenStore.current().access)?.toLongOrNull()
            Log.d("MainVM", "userId: $userId")

            if (userId != null && userId > 0) {
                viewModelScope.launch {
                    Log.d("MainVM", "동기화 시작")

                    val syncOk = syncData(userId)
                    Log.d("MainVM", "동기화 완료, 성공 여부 = $syncOk")

                    if (!syncOk) {
                        Log.e("MainVM", "동기화 실패 → load() 생략")
                        return@launch
                    }

                    Log.d("MainVM", "첫 데이터 대기 중...")
                    getPlansUseCase(userId).first()
                    getRegiHistoriesUseCase().first()

                    Log.d("MainVM", "데이터 확인 완료, load 시작")
                    load(userId)
                    Log.d("MainVM", "load 완료")
                }
            }

            Log.d("MainVM", "FCM 초기화 시작")
            initFcmToken()
            Log.d("MainVM", "타이머 시작")
            startTimeUpdater()
            Log.d("MainVM", "init 완료")
        } catch (e: Exception) {
            Log.e("MainVM", "init 실패", e)
        }
    }

    /**
     * 동기화: suspend + HttpException 요약 로그
     */
    private suspend fun syncData(userId: Long): Boolean {
        return try {
            Log.d("MainVM", "syncData 호출 - userId=$userId")

            regiRepo.syncRegiHistories(userId)
            planRepo.syncPlans(userId)

            Log.d("MainVM", "syncData 성공")
            true
        } catch (e: HttpException) {
            val code = e.code()
            val raw = e.response()?.errorBody()?.string()
            val head = raw?.take(800) // 너무 긴 Django HTML을 처음 800자만 출력
            Log.e(
                "MainVM",
                "동기화 실패 (HttpException) code=$code head=$head",
                e
            )
            false
        } catch (e: Exception) {
            Log.e("MainVM", "동기화 실패 (기타 예외)", e)
            false
        }
    }

    private fun startTimeUpdater() {
        viewModelScope.launch {
            while (true) {
                updateRemainTime()
                kotlinx.coroutines.delay(1_000L)
            }
        }
    }

    private fun updateRemainTime() {
        val next = _nextPlan.value ?: return
        val nextAt = next.takenAt ?: return
        val now = System.currentTimeMillis()

        val diff = nextAt - now
        if (diff < 0) {
            _remainText.value = "00:00"
            return
        }

        val totalMinutes = diff / 1000 / 60
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        _remainText.value = String.format("%02d:%02d", hours, mins)
    }

    private fun load(userId: Long) {
        getRegiHistoriesUseCase()
            .onEach { histories ->
                Log.d("MainVM", "RegiHistory 업데이트: ${histories.size}개")
                _histories.value = histories
                updateNextPlan(_plans.value, histories)
            }
            .launchIn(viewModelScope)

        getPlansUseCase(userId)
            .onEach { plans ->
                Log.d("MainVM", "Plan 업데이트: ${plans.size}개")
                _plans.value = plans
                updateNextPlan(plans, _histories.value)
            }
            .launchIn(viewModelScope)
    }

    override suspend fun extendPlanMinutesSuspend(minutes: Int): Boolean {
        val plan = _nextPlan.value ?: return false
        val oldTime = plan.takenAt ?: return false
        val newTime = oldTime + minutes * 60_000L

        val userId = JwtUtils
            .extractUserId(tokenStore.current().access)
            ?.toLongOrNull()
            ?: return false

        val samePlans = _plans.value.filter {
            it.takenAt == oldTime && it.taken == null
        }

        var allSuccess = true
        samePlans.forEach { p ->
            val updated = p.copy(takenAt = newTime)
            val ok = updatePlanUseCase(userId, updated)
            if (!ok) allSuccess = false
        }

        return allSuccess
    }

    override fun finishPlan() {
        val plan = _nextPlan.value ?: return
        val userId = JwtUtils.extractUserId(tokenStore.current().access)?.toLongOrNull() ?: return

        val now = System.currentTimeMillis()
        val updated = plan.copy(taken = now)

        viewModelScope.launch {
            updatePlanUseCase(userId, updated)
        }
    }

    private fun updateNextPlan(plans: List<Plan>, histories: List<RegiHistory>) {
        Log.d("MainVM", "updateNextPlan 호출 - plans: ${plans.size}, histories: ${histories.size}")

        val now = System.currentTimeMillis()

        val next = plans
            .filter {
                it.takenAt != null &&
                        it.takenAt!! >= now &&
                        it.taken == null
            }
            .minByOrNull { it.takenAt!! }

        Log.d("MainVM", "다음 복용: $next")

        _nextPlan.value = next

        if (next != null) {
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
