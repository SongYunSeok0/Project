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
    private var lastStepsOfDay: Int = 0

    // 하루 업로드 잠금
    private var dailyUploaded = false


    fun checkPermission() = viewModelScope.launch {
        _permissionGranted.value = hc.isGranted()
    }


    fun startAutoUpdateOnce(intervalMillis: Long = 1_000L) {
        if (autoStarted) return
        autoStarted = true
        if (autoJob != null) return

        autoJob = viewModelScope.launch {
            while (isActive) {
                if (_permissionGranted.value) {

                    val v = hc.getTodaySteps().toInt()
                    _todaySteps.value = v

                    val nowDate = LocalDate.now().toString()

                    Log.d(
                        "StepVM",
                        "today=$v, last=$lastStepsOfDay, lastDate=$lastDate, dailyUploaded=$dailyUploaded"
                    )

                    // 그래프/히스토리용 raw steps 저장
                    repo.insertStep(steps = v)


                    // 자정 넘어가는 순간 (하루 1회만)
                    if (nowDate != lastDate && !dailyUploaded) {

                        val yesterday = lastDate
                        val totalYesterday = lastStepsOfDay

                        Log.d("StepVM", "==== 자정 감지 ====")
                        Log.d("StepVM", "기록: $yesterday = $totalYesterday steps")

                        // 로컬 DB 저장
                        repo.saveDailyStep(
                            DailyStep(
                                date = yesterday,
                                steps = totalYesterday
                            )
                        )

                        // 서버 업로드
                        repo.uploadDailyStep(
                            DailyStep(
                                date = yesterday,
                                steps = totalYesterday
                            )
                        )

                        // 히스토리 초기화
                        repo.clearSteps()

                        // 하루 업로드 잠금
                        dailyUploaded = true

                        // 기준 날짜 갱신
                        lastDate = nowDate
                    }


                    // 날짜가 바뀌면 잠금 해제
                    if (nowDate != lastDate) {
                        dailyUploaded = false
                    }

                    lastStepsOfDay = v
                }

                delay(intervalMillis)
            }
        }
    }


    fun requestPermissions() = hc.permissions


    override fun onCleared() {
        super.onCleared()
        autoJob?.cancel()
    }
}
