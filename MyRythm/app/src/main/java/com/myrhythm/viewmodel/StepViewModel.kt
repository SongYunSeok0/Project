package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import com.domain.sharedvm.StepVMContract
import com.myrhythm.health.HealthConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StepViewModel @Inject constructor(
    private val repo: StepRepository,
    private val hc: HealthConnectRepository
) : ViewModel(), StepVMContract {

    private val _permissionGranted = MutableStateFlow(false)
    override val permissionGranted = _permissionGranted.asStateFlow()

    private val _todaySteps = MutableStateFlow(0)
    override val todaySteps = _todaySteps.asStateFlow()

    private var autoJob: Job? = null
    private var autoStarted = false

    private var lastDate: String = LocalDate.now().toString()
    private var lastSteps: Int = 0

    private var dailyUploaded = false

    override fun checkPermission() {
        viewModelScope.launch {
            _permissionGranted.value = hc.isGranted()
        }
    }

    override fun startAutoUpdateOnce(intervalMs: Long) {
        if (autoStarted) return
        autoStarted = true

        autoJob = viewModelScope.launch {
            while (isActive) {
                if (_permissionGranted.value) {

                    val steps = hc.getTodaySteps().toInt()
                    _todaySteps.value = steps

                    val nowDate = LocalDate.now().toString()

                    Log.d("StepVM", "today=$steps last=$lastSteps lastDate=$lastDate uploaded=$dailyUploaded")

                    repo.insertStep(steps)

                    val dateChanged = nowDate != lastDate

                    if (dateChanged && !dailyUploaded) {
                        uploadYesterdaySteps()
                        dailyUploaded = true
                        lastDate = nowDate
                    }

                    if (dateChanged) dailyUploaded = false

                    lastSteps = steps
                }

                delay(intervalMs)
            }
        }
    }

    private suspend fun uploadYesterdaySteps() {
        val yesterday = lastDate
        val steps = lastSteps

        Log.d("StepVM", "==== 자정 처리 ====")
        Log.d("StepVM", "기록: $yesterday = $steps steps")

        val d = DailyStep(date = yesterday, steps = steps)

        repo.saveDailyStep(d)
        repo.uploadDailyStep(d)
        repo.clearSteps()
    }

    override fun requestPermissions() = hc.permissions

    override fun onCleared() {
        autoJob?.cancel()
    }
}
