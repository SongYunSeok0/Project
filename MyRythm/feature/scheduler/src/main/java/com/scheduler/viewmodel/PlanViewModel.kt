package com.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.Plan
import com.domain.model.PlanStatus
import com.domain.model.RegiHistory
import com.domain.usecase.device.GetMyDevicesUseCase
import com.domain.usecase.plan.CreatePlanUseCase
import com.domain.usecase.plan.DeletePlanUseCase
import com.domain.usecase.plan.GetPlanUseCase
import com.domain.usecase.plan.RefreshPlansUseCase      // ✅ 다시 사용
import com.domain.usecase.plan.UpdatePlanUseCase
import com.domain.usecase.plan.MarkMedTakenUseCase
import com.domain.usecase.regi.GetRegiHistoriesUseCase
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import com.scheduler.ui.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import com.scheduler.ui.toUiError

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getPlansUseCase: GetPlanUseCase,
    private val createPlanUseCase: CreatePlanUseCase,
    private val updatePlanUseCase: UpdatePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val refreshPlansUseCase: RefreshPlansUseCase,   // ✅ 추가
    private val getRegiHistoriesUseCase: GetRegiHistoriesUseCase,
    private val getMyDevicesUseCase: GetMyDevicesUseCase,
    private val markMedTakenUseCase: MarkMedTakenUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val histories: List<RegiHistory> = emptyList(),
        val error: UiError? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate = _itemsByDate.asStateFlow()

    private val _isDeviceUser = MutableStateFlow(false)
    val isDeviceUser: StateFlow<Boolean> = _isDeviceUser.asStateFlow()

    fun load(userId: Long) {
        viewModelScope.launch {

            // 기기 연동 여부 체크
            launch {
                when (val result = getMyDevicesUseCase()) {
                    is ApiResult.Success -> {
                        _isDeviceUser.value = result.data.isNotEmpty()
                    }
                    is ApiResult.Failure -> {
                        _isDeviceUser.value = false
                    }
                }
            }


            // RegiHistory + Plan Flow 구독
            getRegiHistoriesUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = UiError.Message(e.message ?: "데이터를 불러오는 중 오류가 발생했어요")
                        )
                    }
                }
                .collect { histories ->

                    _uiState.update { it.copy(histories = histories) }

                    getPlansUseCase(userId)
                        .catch { e ->
                            _uiState.update {
                                it.copy(
                                    error = UiError.Message(e.message ?: "데이터를 불러오는 중 오류가 발생했어요")
                                )
                            }
                        }
                        .collect { plans ->

                            _uiState.update { it.copy(plans = plans) }

                            _itemsByDate.value = makeItemsByDate(plans, histories)
                        }
                }
        }
    }

    // -------------------------------
    // 🔥 복용 완료 처리
    // -------------------------------
    fun markAsTaken(userId: Long, planId: Long) {
        viewModelScope.launch {
            when (val result = markMedTakenUseCase(planId)) {

                is ApiResult.Success -> {
                    when (val refreshResult = refreshPlansUseCase(userId)) {

                        is ApiResult.Success -> {
                            // 성공 시 아무 처리 필요 없음
                        }

                        is ApiResult.Failure -> {
                            _uiState.update {
                                it.copy(
                                    error = refreshResult.error.toUiError()
                                )
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            error = result.error.toUiError()
                        )
                    }
                }
            }
        }
    }



    // -------------------------------
    // 알람 토글
    // -------------------------------
    fun toggleAlarm(userId: Long, planId: Long, newValue: Boolean) {
        viewModelScope.launch {
            val plan = _uiState.value.plans.find { it.id == planId } ?: return@launch
            val updated = plan.copy(useAlarm = newValue)
            updatePlanUseCase(userId, updated)
            // updatePlanUseCase 내부에서 Room을 업데이트하고 있으면
            // Flow를 통해 자동 반영되고, 아니면 여기서도 refreshPlansUseCase(userId)를 한 번 태워도 됨.
        }
    }

    // -------------------------------
    // 화면 표시용 그룹 생성
    // -------------------------------
    private fun makeItemsByDate(
        plans: List<Plan>,
        histories: List<RegiHistory>
    ): Map<LocalDate, List<MedItem>> {

        val zone = ZoneId.systemDefault()

        val labelMap = histories.associateBy(
            { it.id },
            { it.label ?: "" }
        )

        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        plans
            .filter { it.takenAt != null }
            .groupBy { p ->
                val local = Instant.ofEpochMilli(p.takenAt!!).atZone(zone)
                val date = local.toLocalDate()
                val time = local.toLocalTime().toString().substring(0, 5)
                Triple(date, p.regihistoryId, time)
            }
            .forEach { (key, group) ->

                val (date, rhId, time) = key
                val label = labelMap[rhId] ?: group.first().medName ?: "약"
                val representative = group.first()

                val intakeStatus = when {
                    group.any { it.status == PlanStatus.MISSED } ->
                        IntakeStatus.MISSED
                    group.all { it.status == PlanStatus.DONE } ->
                        IntakeStatus.DONE
                    else ->
                        IntakeStatus.SCHEDULED
                }

                val item = MedItem(
                    planIds = group.map { it.id },
                    label = label,
                    medNames = group.map { it.medName },
                    time = time,
                    mealTime = representative.mealTime,
                    memo = representative.note,
                    useAlarm = representative.useAlarm,
                    status = intakeStatus
                )

                out.getOrPut(date) { mutableListOf() }.add(item)
            }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}
