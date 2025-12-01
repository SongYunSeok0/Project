package com.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Plan
import com.domain.model.RegiHistory
import com.domain.usecase.plan.CreatePlanUseCase
import com.domain.usecase.plan.DeletePlanUseCase
import com.domain.usecase.plan.GetPlansUseCase
import com.domain.usecase.plan.RefreshPlansUseCase
import com.domain.usecase.plan.UpdatePlanUseCase
import com.domain.usecase.regi.GetRegiHistoriesUseCase
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
    private val refreshPlansUseCase: RefreshPlansUseCase,
    private val getRegiHistoriesUseCase: GetRegiHistoriesUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val histories: List<RegiHistory> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate = _itemsByDate.asStateFlow()

    fun load(userId: Long) {
        viewModelScope.launch {

            getRegiHistoriesUseCase()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { histories ->

                    _uiState.update { it.copy(histories = histories) }

                    getPlansUseCase(userId)
                        .catch { e ->
                            _uiState.update { it.copy(error = e.message) }
                        }
                        .collect { plans ->

                            _uiState.update { it.copy(plans = plans) }

                            _itemsByDate.value = makeItemsByDate(plans, histories)
                        }
                }
        }
    }

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

    fun updatePlan(userId: Long, plan: Plan) {
        viewModelScope.launch {
            updatePlanUseCase(userId, plan)
        }
    }

    fun deletePlan(userId: Long, planId: Long) {
        viewModelScope.launch {
            deletePlanUseCase(userId, planId)
        }
    }

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
                val rhId = p.regihistoryId
                Triple(date, rhId, time)
            }
            .forEach { (_, group) ->
                val p = group.first()

                val local = Instant.ofEpochMilli(p.takenAt!!).atZone(zone)
                val date = local.toLocalDate()
                val time = local.toLocalTime().toString().substring(0, 5)

                val label = labelMap[p.regihistoryId] ?: p.medName

                val item = MedItem(
                    label = label,
                    time = time,
                    status = IntakeStatus.SCHEDULED
                )

                out.getOrPut(date) { mutableListOf() }.add(item)
            }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}
