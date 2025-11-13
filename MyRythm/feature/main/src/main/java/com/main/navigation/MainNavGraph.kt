package com.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.main.ui.MainScreen
import kotlinx.serialization.ExperimentalSerializationApi

import com.chatbot.navigation.ChatBotRoute
import com.main.ui.AlarmScreen
import com.scheduler.navigation.SchedulerRoute
import com.scheduler.navigation.CameraRoute
import com.mypage.navigation.MyPageRoute
import com.mypage.navigation.HeartReportRoute
import com.map.navigation.MapRoute
import com.mypage.navigation.MediReportRoute
import com.mypage.ui.MediReportScreen
import com.news.navigation.NewsRoute

@OptIn(ExperimentalSerializationApi::class)
fun NavGraphBuilder.mainNavGraph(nav: NavController) {
    composable<MainRoute> {
        MainScreen(
            onOpenChatBot   = { nav.navigate(ChatBotRoute) },
            onOpenScheduler = { nav.navigate(SchedulerRoute) },
            onOpenSteps     = { nav.navigate(MyPageRoute) },          // 임시: 걸음수 → 마이페이지
            onOpenHeart     = { nav.navigate(HeartReportRoute) },   // 마이페이지 상세
            onOpenMap       = { nav.navigate(MapRoute) },
            onOpenNews      = { nav.navigate(NewsRoute) },
            onOpenAlarm      = { nav.navigate(AlarmRoute) },
            onFabCamera     = { nav.navigate(CameraRoute) }         // 처방전 촬영 플로우 시작
        )
    }

    composable<AlarmRoute> {
        AlarmScreen()
    }
}
