package com.mypage.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StepViewModel @Inject constructor(
    private val repository: StepRepository,
    @ApplicationContext private val context: Context
) : ViewModel(), SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var baseSteps: Float? = null

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    private var currentDate: String = todayString()

    init {
        startSensor()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }

    // 센서 시작
    private fun startSensor() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // 센서 종료
    private fun stopSensor() {
        sensorManager.unregisterListener(this)
    }

    // 센서 콜백
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return

        val totalSinceBoot = event.values[0]

        if (baseSteps == null) {
            baseSteps = totalSinceBoot
        }

        val todaySteps = (totalSinceBoot - (baseSteps ?: totalSinceBoot)).toInt()
        _steps.value = todaySteps

        checkDateChange(todaySteps)
        saveSnapshot(todaySteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요 시 사용
    }

    // 오늘 날짜 문자열 생성
    private fun todayString(): String {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(now)
    }

    // 날짜 변경 감지
    private fun checkDateChange(todaySteps: Int) {
        val today = todayString()

        if (today != currentDate) {
            val yesterday = currentDate

            viewModelScope.launch {
                repository.saveDailyStep(
                    date = yesterday,
                    steps = todaySteps
                )
            }

            currentDate = today
            baseSteps = null
            _steps.value = 0
        }
    }

    // 스냅샷 저장
    private fun saveSnapshot(steps: Int) {
        viewModelScope.launch {
            repository.saveSnapshot(
                steps = steps,
                collectedAt = System.currentTimeMillis()
            )
        }
    }
}
