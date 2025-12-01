package com.myrhythm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.sharedvm.HeartRateVMContract
import com.domain.usecase.health.GetLatestHeartRateUseCase
import com.domain.usecase.health.GetHeartHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    private val getLatestHeartRateUseCase: GetLatestHeartRateUseCase,
    private val getHeartHistoryUseCase: GetHeartHistoryUseCase
) : ViewModel(), HeartRateVMContract {

    private val _latestHeartRate = MutableStateFlow<Int?>(null)
    override val latestHeartRate: StateFlow<Int?> = _latestHeartRate

    override fun loadLatestHeartRate() {
        viewModelScope.launch {
            runCatching { getLatestHeartRateUseCase() }
                .onSuccess { bpm -> _latestHeartRate.value = bpm }
        }
    }

    override val heartHistory = getHeartHistoryUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override fun syncHeartHistory() {
        viewModelScope.launch {
            runCatching { getHeartHistoryUseCase.sync() }
        }
    }

    fun start() {
        loadLatestHeartRate()
        syncHeartHistory()
    }
}
