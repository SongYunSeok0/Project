package com.myrhythm.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.ExperimentalSerializationApi
import com.chatbot.navigation.ChatBotRoute
import com.scheduler.navigation.SchedulerRoute
import com.mypage.navigation.HeartReportRoute
import com.map.navigation.MapRoute
import com.mypage.navigation.EditProfileRoute
import com.mypage.viewmodel.MyPageViewModel
import com.news.navigation.NewsRoute
import com.shared.navigation.MainRoute
import com.myrhythm.viewmodel.MainViewModel
import com.myrhythm.viewmodel.HeartRateViewModel
import com.myrhythm.viewmodel.StepViewModel

@OptIn(ExperimentalSerializationApi::class)
fun NavGraphBuilder.mainNavGraph(
    nav: NavController,
    onLogoutClick: () -> Unit
) {
    composable<MainRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MainRoute>()
        val uid = route.userId

        // 여기서 ViewModel 생성
        val mainVm: MainViewModel = hiltViewModel()
        val heartVm: HeartRateViewModel = hiltViewModel()
        val stepVm: StepViewModel = hiltViewModel()
        val myPageViewModel: MyPageViewModel = hiltViewModel()

        StepViewModelRoute(
            myPageViewModel = myPageViewModel,
            mainViewModel = mainVm,
            heartViewModel = heartVm,
            stepViewModel = stepVm,
            onOpenChatBot   = { nav.navigate(ChatBotRoute()) },
            onOpenScheduler = { nav.navigate(SchedulerRoute(uid)) },
            onOpenHeart     = { nav.navigate(HeartReportRoute) },
            onOpenMap       = { nav.navigate(MapRoute) },
            onOpenNews      = { nav.navigate(NewsRoute(uid)) },
            onOpenEditScreen = { nav.navigate(EditProfileRoute) }
        )
    }
}