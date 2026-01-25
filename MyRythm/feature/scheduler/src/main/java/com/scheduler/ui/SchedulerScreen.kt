package com.scheduler.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.scheduler.viewmodel.PlanViewModel
import com.shared.navigation.MainRoute

// 데이터 모델
enum class IntakeStatus { DONE, SCHEDULED, MISSED }

data class MedItem(
    val planIds: List<Long>,
    val label: String,
    val medNames: List<String>,
    val time: String,
    val mealTime: String?,
    val memo: String?,
    val useAlarm: Boolean,
    val status: IntakeStatus
)

//  외부에서 호출되는 Screen
@Composable
fun SchedulerScreen(
    userId: Long,
    navController: NavController,
    vm: PlanViewModel = hiltViewModel(),
    onOpenRegi: () -> Unit = {}
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val items by vm.itemsByDate.collectAsStateWithLifecycle()
    val isDeviceUser by vm.isDeviceUser.collectAsStateWithLifecycle()

    BackHandler {
        navController.navigate(MainRoute) {
            popUpTo(MainRoute) { inclusive = false }
            launchSingleTop = true
        }
    }

    LaunchedEffect(userId) {
        if (userId > 0L) vm.load(userId)
        else Log.e("SchedulerScreen", " userId 누락: '$userId'")
    }

    SchedulerContent(
        itemsByDate = items,
        resetKey = ui.plans.hashCode(),
        isDeviceUser = isDeviceUser,
        onToggleAlarm = { planIds, newValue ->
            planIds.forEach { planId ->
                vm.toggleAlarm(userId, planId, newValue)
            }
        },
        onMarkTaken = { planIds ->
            // 한 카드에 묶인 모든 Plan을 복용 완료 처리
            planIds.forEach { planId ->
                vm.markAsTaken(userId, planId)
            }
        }
    )
}