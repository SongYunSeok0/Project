package com.domain.usecase.health

import com.domain.model.HeartRateHistory
import com.domain.repository.HeartRateRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// 🔥 UI용 데이터 모델 (String date)
data class DailyHeartRateUI(
    val date: String,
    val measurements: List<Int>
)

class GetWeeklyHeartRatesUseCase @Inject constructor(
    private val heartRateRepository: HeartRateRepository
) {
    suspend operator fun invoke(): List<DailyHeartRateUI> {
        val heartRates = heartRateRepository.getWeeklyHeartRates()
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return heartRates
            .groupBy { history ->
                try {
                    Instant.parse(history.collectedAt)
                        .atZone(zone)
                        .toLocalDate()
                } catch (e: Exception) {
                    LocalDate.now()
                }
            }
            .map { (date, rates) ->
                DailyHeartRateUI(
                    date = date.format(formatter),  // 🔥 String으로 변환
                    measurements = rates.map { it.bpm }.take(3)
                )
            }
            .sortedBy { it.date }
    }
}