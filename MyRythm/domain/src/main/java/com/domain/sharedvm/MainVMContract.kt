package com.domain.sharedvm

import kotlinx.coroutines.flow.StateFlow
import com.domain.model.Plan

interface MainVMContract {
    val nextPlan: StateFlow<Plan?>
    val nextLabel: StateFlow<String?>
    val remainText: StateFlow<String?>
    val previewExtendMinutes: StateFlow<Int>

    fun finishPlan()
    fun clearPreview()
    fun previewExtend(min: Int)

    suspend fun extendPlanMinutesSuspend(min: Int): Boolean
}
