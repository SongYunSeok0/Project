package com.myrhythm.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypage.viewmodel.StepViewModel
import com.mypage.viewmodel.MyPageViewModel
import com.myrhythm.viewmodel.MainViewModel
import com.shared.ui.MainScreen

@Composable
fun StepViewModelRoute(
    myPageViewModel: MyPageViewModel,
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenSteps: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onFabCamera: () -> Unit = {},
    onOpenEditScreen: () -> Unit = {},   // ⭐ EditScreen 이동 콜백
) {
    val stepViewModel: StepViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()

    val steps by stepViewModel.steps.collectAsStateWithLifecycle()
    val remainText by mainViewModel.remainText.collectAsStateWithLifecycle()
    val profile by myPageViewModel.profile.collectAsStateWithLifecycle()
    var showGuardianDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        val p = profile

        if (p == null) return@LaunchedEffect

        if (p.prot_email.isNullOrBlank()) {
            showGuardianDialog = true
        } else {
            showGuardianDialog = false
        }
    }



    if (showGuardianDialog) {
        AlertDialog(
            onDismissRequest = { /* 뒤로가기 막기 */ },

            title = {
                Text("추가 정보가 필요해요 😊")
            },

            text = {
                Text("원활한 사용을 위해 보호자 이메일을 입력해 주세요!")
            },

            confirmButton = {
                Text(
                    text = "정보 입력하기",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showGuardianDialog = false
                            onOpenEditScreen()
                        }
                )
            }
        )
    }

    MainScreen(
        onOpenChatBot = onOpenChatBot,
        onOpenScheduler = onOpenScheduler,
        onOpenSteps = onOpenSteps,
        onOpenHeart = onOpenHeart,
        onOpenMap = onOpenMap,
        onOpenNews = onOpenNews,
        onFabCamera = onFabCamera,
        todaySteps = steps,
        remainText = remainText
    )
}
