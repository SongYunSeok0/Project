package com.myrhythm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.ExperimentalSerializationApi
import com.chatbot.navigation.ChatBotRoute
import com.scheduler.navigation.SchedulerRoute
import com.scheduler.navigation.CameraRoute
import com.mypage.navigation.MyPageRoute
import com.mypage.navigation.HeartReportRoute
import com.map.navigation.MapRoute
import com.news.navigation.NewsRoute
import com.shared.navigation.MainRoute

@OptIn(ExperimentalSerializationApi::class)
fun NavGraphBuilder.mainNavGraph(nav: NavController, userId: String) {
    composable<MainRoute> {
        StepViewModelRoute(
            onOpenChatBot   = { nav.navigate(ChatBotRoute()) },
            onOpenScheduler = { nav.navigate(SchedulerRoute(userId)) },
            onOpenSteps     = { nav.navigate(MyPageRoute(userId)) },
            onOpenHeart     = { nav.navigate(HeartReportRoute(userId)) },
            onOpenMap       = { nav.navigate(MapRoute) },
            onOpenNews      = { nav.navigate(NewsRoute) },
            onFabCamera     = { nav.navigate(CameraRoute) }
        )
    }
}
