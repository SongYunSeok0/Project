package com.domain.sharedvm

import kotlinx.coroutines.flow.StateFlow

interface StepVMContract {
    val todaySteps: StateFlow<Int>
    val permissionGranted: StateFlow<Boolean>

    fun requestPermissions(): Set<String>
    fun checkPermission()
    fun startAutoUpdateOnce(intervalMs: Long = 5000)
}
