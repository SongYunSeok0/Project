package com.main

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.navigation.*

import com.navigation.ChatBotRoute
import com.navigation.SchedulerRoute
import com.navigation.MyPageRoute
import com.navigation.MapRoute
import com.navigation.NewsRoute

fun NavGraphBuilder.mainNavGraph(nav: NavController) {
    composable<MainRoute> {
        MainScreen(
            onOpenChatBot   = { nav.navigate(ChatBotRoute) },
            onOpenScheduler = { nav.navigate(SchedulerRoute) },
            onOpenSteps     = { nav.navigate(MyPageRoute) },      // 걸음수 → 임시로 마이페이지
            onOpenHeart     = { nav.navigate(HeartReportRoute()) },      // 최근 심박 수 → 임시로 마이페이지
            onOpenMap       = { nav.navigate(MapRoute) },
            onOpenNews      = { nav.navigate(NewsRoute) },
            onFabCamera     = { nav.navigate(CameraRoute()) }       // 가운데 알약 FAB
        )
    }
}
