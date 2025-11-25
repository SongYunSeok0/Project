package com.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Plan
import com.domain.usecase.plan.CreatePlanUseCase
import com.domain.usecase.plan.DeletePlanUseCase
import com.domain.usecase.plan.GetPlansUseCase
import com.domain.usecase.plan.RefreshPlansUseCase
import com.domain.usecase.plan.UpdatePlanUseCase
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getPlansUseCase: GetPlansUseCase,
    private val createPlanUseCase: CreatePlanUseCase,
    private val updatePlanUseCase: UpdatePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val refreshPlansUseCase: RefreshPlansUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate = _itemsByDate.asStateFlow()

    // ----------------------------------------------------
    // 🔥 전체 Plan 로드
    // ----------------------------------------------------
    fun load(userId: Long) {
        viewModelScope.launch {
            getPlansUseCase(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(plans = list) }
                    _itemsByDate.value = makeItemsByDate(list)
                }
        }
    }

    // ----------------------------------------------------
    // 🔥 Plan 생성
    // ----------------------------------------------------
    fun createPlan(
        regihistoryId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?,
        useAlarm: Boolean
    ) {
        viewModelScope.launch {
            createPlanUseCase(
                regihistoryId,
                medName,
                takenAt,
                mealTime,
                note,
                taken,
                useAlarm
            )
        }
    }

    // ----------------------------------------------------
    // 🔥 Plan 수정
    // ----------------------------------------------------
    fun updatePlan(userId: Long, plan: Plan) {
        viewModelScope.launch {
            updatePlanUseCase(userId, plan)
        }
    }

    // ----------------------------------------------------
    // 🔥 Plan 삭제
    // ----------------------------------------------------
    fun deletePlan(userId: Long, planId: Long) {
        viewModelScope.launch {
            deletePlanUseCase(userId, planId)
        }
    }

    // ----------------------------------------------------
    // 🔄 날짜별 변환
    // ----------------------------------------------------
    private fun makeItemsByDate(plans: List<Plan>): Map<LocalDate, List<MedItem>> {
        val zone = ZoneId.systemDefault()
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        plans.forEach { p ->
            val takenAt = p.takenAt ?: return@forEach
            val instant = Instant.ofEpochMilli(takenAt)
            val local = instant.atZone(zone)
            val date = local.toLocalDate()
            val time = local.toLocalTime().toString().substring(0, 5)

            val item = MedItem(
                name = p.medName,
                time = time,
                status = IntakeStatus.SCHEDULED
            )
            out.getOrPut(date) { mutableListOf() }.add(item)
        }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}
