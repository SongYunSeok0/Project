package com.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.MealRelation
import com.domain.model.Plan
import com.domain.model.PlanType
import com.domain.usecase.plan.CreatePlanUseCase
import com.domain.usecase.plan.DeletePlanUseCase
import com.domain.usecase.plan.GetPlansUseCase
import com.domain.usecase.plan.RefreshPlansUseCase
import com.domain.usecase.plan.UpdatePlanUseCase
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getPlans: GetPlansUseCase,
    private val refreshPlans: RefreshPlansUseCase,
    private val createPlan: CreatePlanUseCase,
    private val updatePlan: UpdatePlanUseCase,
    private val deletePlan: DeletePlanUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate: StateFlow<Map<LocalDate, List<MedItem>>> = _itemsByDate.asStateFlow()

    private var observeJob: Job? = null

    fun load(userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            // observe
            launch {
                getPlans(userId)
                    .catch { e -> _uiState.update { it.copy(error = e.message) } }
                    .collect { list ->
                        _uiState.update { it.copy(plans = list) }
                        _itemsByDate.value = makeItemsByDate(list)
                    }
            }

            // refresh
            runCatching { refreshPlans(userId) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }

            _uiState.update { it.copy(loading = false) }
        }
    }

    fun create(
        userId: String,
        type: PlanType,
        diseaseName: String?,
        supplementName: String?,
        dosePerDay: Int,
        mealRelation: MealRelation?,
        memo: String?,
        startDay: Long,
        endDay: Long?,
        meds: List<String>,
        times: List<String>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val plan = Plan(
                id = 0L,
                type = type,
                diseaseName = diseaseName,
                supplementName = supplementName,
                dosePerDay = dosePerDay,
                mealRelation = mealRelation,
                memo = memo,
                startDay = startDay,
                endDay = endDay,
                meds = if (type == PlanType.DISEASE) meds.filter { it.isNotBlank() } else emptyList(),
                times = times
            )
            runCatching { createPlan(userId, plan) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun update(userId: String, plan: Plan) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { updatePlan(userId, plan) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun delete(userId: String, planId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { deletePlan(userId, planId) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun makeItemsByDate(
        plans: List<Plan>,
        zone: ZoneId = ZoneId.systemDefault()
    ): Map<LocalDate, List<MedItem>> {
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        fun dateRange(startMs: Long, endMs: Long?): Sequence<LocalDate> {
            val s = Instant.ofEpochMilli(startMs).atZone(zone).toLocalDate()
            val e = Instant.ofEpochMilli(endMs ?: startMs).atZone(zone).toLocalDate()
            return generateSequence(s) { prev -> if (prev.isBefore(e)) prev.plusDays(1) else null }
                .plus(e)
        }

        plans.forEach { p ->
            val names =
                if (p.type == PlanType.DISEASE)
                    (p.meds.takeIf { it.isNotEmpty() } ?: listOf(p.diseaseName ?: "약"))
                else listOf(p.supplementName ?: "영양제")

            val times = p.times.ifEmpty { listOf("08:00") }

            dateRange(p.startDay, p.endDay).forEach { day ->
                val bucket = out.getOrPut(day) { mutableListOf() }
                names.forEach { n ->
                    times.forEach { t ->
                        bucket += MedItem(
                            name = n,
                            time = t,
                            status = IntakeStatus.SCHEDULED
                        )
                    }
                }
            }
        }
        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}
