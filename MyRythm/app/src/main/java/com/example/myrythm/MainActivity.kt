package com.example.myrythm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.design.AppBottomBar
import com.example.design.AppTopBar
import com.example.myrythm.navigation.AppNavGraph
import com.example.myrythm.navigation.Routes
import com.example.myrythm.ui.theme.MyRythmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyRythmTheme { AppRoot() } }
    }
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: Routes.LOGIN

    val isAuth = route in setOf(Routes.LOGIN, Routes.PWD, Routes.SIGNUP)
    val isMain = route == Routes.MAIN
    val isMap  = route == Routes.MAP
    val isNews = route == Routes.NEWS

    val hideTopBar = isAuth || isMain || isMap || isNews
    val hideBottomBar = isAuth

    fun goHome() {
        nav.navigate(Routes.MAIN) {
            popUpTo(nav.graph.startDestinationId) {
                saveState = true
                inclusive = false
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun goMyPage() {
        nav.navigate(Routes.MYPAGE) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun goScheduleFlow() {
        // 알약 버튼은 흐름의 시작(Camera)로 이동
        nav.navigate(Routes.CAMERA) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                AppTopBar(
                    title = titleFor(route),
                    showBack = true,
                    onBackClick = {
                        if (nav.previousBackStackEntry != null) {
                            nav.popBackStack()
                        } else {
                            goHome()
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!hideBottomBar) {
                AppBottomBar(
                    currentScreen = tabFor(route),
                    onTabSelected = { tab ->
                        when (tab) {
                            "Home"    -> goHome()
                            "MyPage"  -> goMyPage()
                            "Schedule"-> goScheduleFlow()
                        }
                    }
                )
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            AppNavGraph(navController = nav)
        }
    }
}

private fun titleFor(route: String) = when (route) {
    Routes.MYPAGE    -> "마이페이지"
    Routes.SCHEDULER -> "일정"
    Routes.REGI      -> "처방전 등록"
    Routes.CAMERA    -> "카메라"
    Routes.OCR       -> "처방전 인식"
    Routes.HEART     -> "심박수"
    Routes.EDIT      -> "내 정보 수정"
    Routes.CHATBOT   -> "챗봇"
    else -> "마이 리듬"
}

private fun tabFor(route: String) = when (route) {
    Routes.MYPAGE -> "MyPage"
    Routes.SCHEDULER, Routes.CAMERA, Routes.OCR, Routes.REGI -> "Schedule"
    Routes.MAIN -> "Home"
    else -> "Other"
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRythmTheme { AppRoot() }
}
