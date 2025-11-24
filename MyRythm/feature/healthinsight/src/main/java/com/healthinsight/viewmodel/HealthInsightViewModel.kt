package com.healthinsight.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class HealthInsightViewModel @Inject constructor(
    private val stepRepository: StepRepository
) : ViewModel() {

    private val _weeklySteps = MutableStateFlow<List<DailyStep>>(emptyList())
    val weeklySteps: StateFlow<List<DailyStep>> = _weeklySteps

    fun loadAll() {
        loadWeeklySteps()
    }

    private fun loadWeeklySteps() {
        viewModelScope.launch {
            _weeklySteps.value = stepRepository.getWeeklySteps()
        }
    }
}
