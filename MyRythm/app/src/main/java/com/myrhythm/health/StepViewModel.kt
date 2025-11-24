package com.myrhythm.health

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

    // 🔹 자정 체크용: 마지막으로 본 날짜
    private var lastDate: String = LocalDate.now().toString()

    // 🔹 “어제 하루 총 걸음수”를 기억하기 위한 값
    //   (루프 돌 때마다 최신 값으로 덮어씀)
    private var lastStepsOfDay: Int = 0

    fun checkPermission() = viewModelScope.launch {
        _permissionGranted.value = hc.isGranted()
    }

    fun loadTodaySteps() = viewModelScope.launch {
        if (!_permissionGranted.value) return@launch
        _todaySteps.value = hc.getTodaySteps().toInt()
    }

    /**
     * 실시간 업데이트 (기본 1초마다)
     * - todaySteps UI 갱신
     * - steps 테이블 insert(시간까지 저장)
     * - 날짜 변경 감지 → 어제 총 걸음수 daily_steps + 서버 업로드
     */
    fun startAutoUpdate(intervalMillis: Long = 1_000L) {
        if (autoJob != null) return    // 이미 돌고 있으면 다시 시작 X

        autoJob = viewModelScope.launch {
            while (isActive) {
                if (_permissionGranted.value) {

                    // 1) Health Connect 에서 오늘 걸음수 읽기
                    val v = hc.getTodaySteps().toInt()
                    _todaySteps.value = v

                    val nowDate = LocalDate.now().toString()
                    // 2) steps 테이블에 실시간 저장
                    //    (그래프용, 히스토리용)
                    repo.insertStep(
                        steps = v,
                    )

                    // 3) 자정 넘어감 감지
                    if (nowDate != lastDate) {
                        val yesterday = lastDate
                        val totalYesterday = lastStepsOfDay  // 어제 마지막 값

                        // daily_steps 저장
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

                        // 원하면 히스토리 테이블 비우기
                        repo.clearSteps()

                        // 기준 날짜 갱신
                        lastDate = nowDate
                    }

                    // 4) “해당 날짜에서 현재까지 총 걸음수”를 계속 기억
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
