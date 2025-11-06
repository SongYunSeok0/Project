package com.scheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.MealRelation
import com.domain.model.Plan
import com.domain.model.PlanType
import com.domain.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planRepository: PlanRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun load(userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            // 로컬 구독
            launch {
                planRepository.observePlans(userId)
                    .catch { e -> _uiState.update { it.copy(error = e.message) } }
                    .collect { list -> _uiState.update { it.copy(plans = list) } }
            }

            // 원격 새로고침
            runCatching { planRepository.refresh(userId) }
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

            runCatching { planRepository.create(userId, plan) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }

            _uiState.update { it.copy(loading = false) }
        }
    }

    fun update(userId: String, plan: Plan) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { planRepository.update(userId, plan) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun delete(userId: String, planId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { planRepository.delete(userId, planId) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(loading = false) }
        }
    }
}
