package com.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
    composable<MainRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MainRoute>()
        val userId = route.userId

        MainScreen(
            onOpenScheduler = { nav.navigate(SchedulerRoute(userId)) },
            onFabCamera = { nav.navigate(CameraRoute(userId)) },
            onOpenChatBot = { nav.navigate(ChatBotRoute) },
            onOpenSteps = { nav.navigate(MyPageRoute) },
            onOpenHeart = { nav.navigate(HeartReportRoute) },
            onOpenMap = { nav.navigate(MapRoute) },
            onOpenNews = { nav.navigate(NewsRoute) },
            onOpenAlarm      = { nav.navigate(AlarmRoute) },
            )
    }

    composable<AlarmRoute> {
        AlarmScreen()
    }
}
