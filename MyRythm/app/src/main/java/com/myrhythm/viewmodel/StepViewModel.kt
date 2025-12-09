package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import com.domain.sharedvm.StepVMContract
import com.domain.usecase.health.GetWeeklyStepsUseCase
import com.myrhythm.health.HealthConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StepViewModel @Inject constructor(
    private val repo: StepRepository,
    private val hc: HealthConnectRepository,
    private val getWeeklyStepsUseCase: GetWeeklyStepsUseCase  // 🔥 UseCase 주입
) : ViewModel(), StepVMContract {

    private val _permissionGranted = MutableStateFlow(false)
    override val permissionGranted = _permissionGranted.asStateFlow()

    private val _todaySteps = MutableStateFlow(0)
    override val todaySteps = _todaySteps.asStateFlow()

    // 🔥 일주일치 걸음 수 데이터
    private val _weeklySteps = MutableStateFlow<List<DailyStep>>(emptyList())
    val weeklySteps: StateFlow<List<DailyStep>> = _weeklySteps.asStateFlow()

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

        // 🔥 업로드 후 주간 데이터 갱신
        loadWeeklySteps()
    }

    // 🔥 일주일치 걸음 수 데이터 로드
    fun loadWeeklySteps() {
        viewModelScope.launch {
            try {
                val steps = getWeeklyStepsUseCase()  // 🔥 UseCase 사용
                _weeklySteps.value = steps
                Log.d("StepVM", "Weekly steps loaded: ${steps.size} days")
            } catch (e: Exception) {
                Log.e("StepVM", "Failed to load weekly steps", e)
                _weeklySteps.value = emptyList()
            }
        }
    }

    override fun requestPermissions() = hc.permissions

    override fun onCleared() {
        autoJob?.cancel()
    }

    fun insertTestData() {
        viewModelScope.launch {
            try {
                repo.insertDummyData()
                loadWeeklySteps()
                Log.d("StepVM", "✅ 테스트 데이터 7건 삽입 완료")
            } catch (e: Exception) {
                Log.e("StepVM", "❌ 테스트 데이터 삽입 실패", e)
            }
        }
    }
}