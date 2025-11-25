package com.myrhythm.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mypage.viewmodel.StepViewModel
import com.shared.ui.MainScreen
import androidx.compose.runtime.getValue
import com.myrhythm.viewmodel.MainViewModel

@Composable
fun StepViewModelRoute(
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenSteps: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onFabCamera: () -> Unit = {}
) {
    val stepViewModel: StepViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()

    val steps by stepViewModel.steps.collectAsStateWithLifecycle()
    val nextTime by mainViewModel.nextTime.collectAsStateWithLifecycle()
    val remainText by mainViewModel.remainText.collectAsStateWithLifecycle()

    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenSteps = onOpenSteps,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,
        onFabCamera = onFabCamera,
        todaySteps = steps,
        nextTime = nextTime,
        remainText = remainText,
    )
}
