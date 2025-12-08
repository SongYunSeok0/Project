package com.healthinsight.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthInsightViewModel @Inject constructor(
    private val getWeeklyStepsUseCase: GetWeeklyStepsUseCase,
    private val getWeeklyHeartRatesUseCase: GetWeeklyHeartRatesUseCase,
    private val getMedicationDelaysUseCase: GetMedicationDelaysUseCase,
    private val insertDummyStepsUseCase: InsertDummyStepsUseCase
) : ViewModel() {

    private val _weeklySteps = MutableStateFlow<List<DailyStep>>(emptyList())
    val weeklySteps: StateFlow<List<DailyStep>> = _weeklySteps

    private val _weeklyHeartRates = MutableStateFlow<List<DailyHeartRateUI>>(emptyList())
    val weeklyHeartRates: StateFlow<List<DailyHeartRateUI>> = _weeklyHeartRates

    private val _medicationDelays = MutableStateFlow<List<MedicationDelayUI>>(emptyList())
    val medicationDelays: StateFlow<List<MedicationDelayUI>> = _medicationDelays

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true

            loadWeeklySteps()
            loadWeeklyHeartRates()
            loadMedicationDelays()

            _isLoading.value = false
        }
    }

    private fun loadWeeklySteps() {
        viewModelScope.launch {
            _weeklySteps.value = getWeeklyStepsUseCase()
        }
    }

    private fun loadWeeklyHeartRates() {
        viewModelScope.launch {
            _weeklyHeartRates.value = getWeeklyHeartRatesUseCase()
        }
    }

    private fun loadMedicationDelays() {
        viewModelScope.launch {
            _medicationDelays.value = getMedicationDelaysUseCase()
        }
    }

    fun insertTestData() {
        viewModelScope.launch {
            insertDummyStepsUseCase()
            loadWeeklySteps()
        }
    }
}