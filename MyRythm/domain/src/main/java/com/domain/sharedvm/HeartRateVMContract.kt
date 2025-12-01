package com.domain.sharedvm

import com.domain.model.HeartRateHistory
import kotlinx.coroutines.flow.StateFlow

interface HeartRateVMContract {
    val latestHeartRate: StateFlow<Int?>
    val heartHistory: StateFlow<List<HeartRateHistory>>

    fun loadLatestHeartRate()
    fun syncHeartHistory()
}
