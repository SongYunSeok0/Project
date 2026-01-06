package com.healthinsight.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.DailyStep
import com.domain.usecase.health.DailyHeartRateUI
import com.domain.usecase.health.GetWeeklyHeartRatesUseCase
import com.domain.usecase.health.GetWeeklyStepsUseCase
import com.domain.usecase.step.InsertDummyStepsUseCase
import com.domain.usecase.plan.GetMedicationDelaysUseCase
import com.domain.usecase.plan.MedicationDelayUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthInsightViewModel @Inject constructor(
    private val getWeeklyStepsUseCase: GetWeeklyStepsUseCase,
    private val getWeeklyHeartRatesUseCase: GetWeeklyHeartRatesUseCase,
    private val getMedicationDelaysUseCase: GetMedicationDelaysUseCase,
    private val insertDummyStepsUseCase: InsertDummyStepsUseCase
) : ViewModel() {

    data class UiState(
        val weeklySteps: List<DailyStep> = emptyList(),
        val weeklyHeartRates: List<DailyHeartRateUI> = emptyList(),
        val medicationDelays: List<MedicationDelayUI> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            loadWeeklySteps()
            loadWeeklyHeartRates()
            loadMedicationDelays()

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadWeeklySteps() {
        when (val result = getWeeklyStepsUseCase()) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(weeklySteps = result.data) }
            }
            is ApiResult.Failure -> {
                _uiState.update { it.copy(errorMessage = "걸음 수 데이터를 불러올 수 없습니다") }
            }
        }
    }

    private suspend fun loadWeeklyHeartRates() {
        when (val result = getWeeklyHeartRatesUseCase()) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(weeklyHeartRates = result.data) }
            }
            is ApiResult.Failure -> {
                _uiState.update { it.copy(errorMessage = "심박수 데이터를 불러올 수 없습니다") }
            }
        }
    }

    private suspend fun loadMedicationDelays() {
        when (val result = getMedicationDelaysUseCase()) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(medicationDelays = result.data) }
            }
            is ApiResult.Failure -> {
                _uiState.update { it.copy(errorMessage = "복약 지연 데이터를 불러올 수 없습니다") }
            }
        }
    }

    fun insertTestData() {
        viewModelScope.launch {
            val result = insertDummyStepsUseCase()
            when (result) {
                is ApiResult.Success -> {
                    loadWeeklySteps()
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(errorMessage = "테스트 데이터 삽입 실패") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}