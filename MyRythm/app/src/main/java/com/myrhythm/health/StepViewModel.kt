package com.myrhythm.health

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
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
) : ViewModel() {

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.asStateFlow()

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps = _todaySteps.asStateFlow()

    private var autoJob: Job? = null
    private var autoStarted = false

    private var lastDate: String = LocalDate.now().toString()
    private var lastSteps: Int = 0

    private var dailyUploaded = false


    fun checkPermission() = viewModelScope.launch {
        _permissionGranted.value = hc.isGranted()
    }


    fun startAutoUpdateOnce(intervalMillis: Long = 1_000L) {
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

                    // 날짜 변경 감지
                    val dateChanged = nowDate != lastDate

                    // 새 날이 되었고 아직 업로드 안 한 경우
                    if (dateChanged && !dailyUploaded) {
                        uploadYesterdaySteps()
                        dailyUploaded = true
                        lastDate = nowDate
                    }

                    // 날짜가 바뀌었으면 잠금 해제
                    if (dateChanged) dailyUploaded = false

                    lastSteps = steps
                }

                delay(intervalMillis)
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


    fun requestPermissions() = hc.permissions


    override fun onCleared() {
        autoJob?.cancel()
    }
}
