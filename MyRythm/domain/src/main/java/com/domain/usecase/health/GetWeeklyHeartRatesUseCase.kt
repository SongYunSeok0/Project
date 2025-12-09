package com.domain.usecase.health

import com.domain.model.HeartRateHistory
import com.domain.repository.HeartRateRepository
import java.time.LocalDate
import java.time.LocalDateTime
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

        // "yyyy-MM-dd HH:mm:ss" 형식 파서
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val grouped = heartRates
            .groupBy { history ->
                try {
                    // "2025-12-02 08:30:00" -> LocalDate
                    LocalDateTime.parse(history.collectedAt, dateTimeFormatter)
                        .toLocalDate()
                } catch (e: Exception) {
                    // 파싱 실패 시 날짜 부분만 추출 ("2025-12-02")
                    try {
                        LocalDate.parse(history.collectedAt.substring(0, 10))
                    } catch (e2: Exception) {
                        println("Failed to parse: ${history.collectedAt}")
                        LocalDate.now()
                    }
                }
            }

        println("Total records: ${heartRates.size}")
        println("Grouped into ${grouped.size} days")
        grouped.forEach { (date, rates) ->
            println("Date: $date, Count: ${rates.size}")
        }

        return grouped
            .map { (date, rates) ->
                DailyHeartRateUI(
                    date = date.format(dateFormatter),
                    measurements = rates.map { it.bpm }
                )
            }
            .sortedBy { it.date }
    }
}