package com.myrhythm.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject

class HealthConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var client: HealthConnectClient? = null

    fun ensureClient(): Boolean {
        if (client != null) return true

        return try {
            client = HealthConnectClient.getOrCreate(context)
            true
        } catch (e: Exception) {
            android.util.Log.e("HC", "ensureClient 예외 = ${e.message}")
            false
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    // ✅ 삼성헬스 관련 조건 싹 제거
    suspend fun isGranted(): Boolean {
        android.util.Log.e("HC", "isGranted() 호출됨")

        if (!ensureClient()) {
            android.util.Log.e("HC", "isGranted() → ensureClient 실패")
            return false
        }

        val granted = client!!.permissionController.getGrantedPermissions()
        android.util.Log.e("HC", "isGranted() → grantedPermissions = $granted")
        android.util.Log.e("HC", "isGranted() → 필요 permissions = $permissions")

        val hasPermissions = granted.containsAll(permissions)
        android.util.Log.e("HC", "isGranted() → HC 권한 충분? = $hasPermissions")
        return hasPermissions
    }

    suspend fun getTodaySteps(): Long {
        if (!ensureClient()) {
            android.util.Log.e("HC", "getTodaySteps() → ensureClient 실패")
            return 0
        }

        val now = LocalDateTime.now()
        val start = now.toLocalDate().atStartOfDay()

        val response = client!!.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, now)
            )
        )

        val sum = response.records.sumOf { it.count.toLong() }
        android.util.Log.e("HC", "getTodaySteps() = $sum, records = ${response.records.size}")

        return sum
    }
}
