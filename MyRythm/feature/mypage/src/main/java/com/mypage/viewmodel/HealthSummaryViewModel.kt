package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.usecase.health.GetLatestHeartRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HealthSummaryViewModel @Inject constructor(
    private val getLatestHeartRateUseCase: GetLatestHeartRateUseCase
) : ViewModel() {

    private val _latestHeartRate = MutableStateFlow<Int?>(null)
    val latestHeartRate: StateFlow<Int?> = _latestHeartRate.asStateFlow()

    init {
        loadLatestHeartRate()
    }

    fun loadLatestHeartRate() = viewModelScope.launch {
        Log.e("HealthSummaryVM", "💓 최근 심박수 로드 시작")

        when (val result = getLatestHeartRateUseCase()) {
            is ApiResult.Success -> {
                Log.e("HealthSummaryVM", "✅ 심박수: ${result.data}")
                _latestHeartRate.value = result.data
            }
            is ApiResult.Failure -> {
                Log.e("HealthSummaryVM", "❌ 심박수 로드 실패: ${result.error}")
                _latestHeartRate.value = null
            }
        }
    }
}
