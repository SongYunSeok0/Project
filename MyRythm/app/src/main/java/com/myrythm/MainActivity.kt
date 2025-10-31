package com.myrythm

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.design.AppBottomBar
import com.design.AppTopBar
import com.myrythm.ui.theme.MyRythmTheme

// 각 feature 모듈 NavGraph
import com.auth.navigation.*
import com.main.navigation.*
import com.map.navigation.*
import com.news.navigation.*
import com.scheduler.navigation.*
import com.mypage.navigation.*
import com.chatbot.navigation.*

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
    val routeName = backStack?.destination?.route.orEmpty()

    // 현재 라우트 판별
    fun isRoute(obj: Any) = routeName == obj::class.qualifiedName
    fun isOf(vararg objs: Any) = objs.any { isRoute(it) }

    val isAuth = isOf(LoginRoute, PwdRoute, SignupRoute)
    val isMain = isRoute(MainRoute)
    val isMap  = isRoute(MapRoute)
    val isNews = isRoute(NewsRoute)

    val hideTopBar = isAuth || isMain || isMap || isNews
    val hideBottomBar = isAuth

    // 탭 이동
    fun goHome() = nav.navigate(MainRoute) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    fun goMyPage() = nav.navigate(MyPageRoute) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    fun goScheduleFlow() = nav.navigate(CameraRoute) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                AppTopBar(
                    title = titleFor(routeName),
                    showBack = true,
                    onBackClick = {
                        if (nav.previousBackStackEntry != null) nav.popBackStack()
                        else goHome()
                    }
                )
            }
        },
        bottomBar = {
            if (!hideBottomBar) {
                AppBottomBar(
                    currentScreen = tabFor(routeName),
                    onTabSelected = { tab ->
                        when (tab) {
                            "Home"     -> goHome()
                            "MyPage"   -> goMyPage()
                            "Schedule" -> goScheduleFlow()
                        }
                    }
                )
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            NavHost(navController = nav, startDestination = AuthGraph) {
                authNavGraph(nav)
                mainNavGraph(nav)
                mapNavGraph()
                newsNavGraph(nav)
                schedulerNavGraph(nav)
                mypageNavGraph(nav)
                chatbotNavGraph()
            }
        }
    }
}

private fun titleFor(routeName: String) = when (routeName) {
    MyPageRoute::class.qualifiedName      -> "마이페이지"
    SchedulerRoute::class.qualifiedName   -> "일정"
    RegiRoute::class.qualifiedName        -> "처방전 등록"
    CameraRoute::class.qualifiedName      -> "카메라"
    OcrRoute::class.qualifiedName         -> "처방전 인식"
    HeartReportRoute::class.qualifiedName -> "심박수"
    EditProfileRoute::class.qualifiedName -> "내 정보 수정"
    ChatBotRoute::class.qualifiedName     -> "챗봇"
    else -> "마이 리듬"
}

private fun tabFor(routeName: String) = when (routeName) {
    MyPageRoute::class.qualifiedName -> "MyPage"
    SchedulerRoute::class.qualifiedName,
    CameraRoute::class.qualifiedName,
    OcrRoute::class.qualifiedName,
    RegiRoute::class.qualifiedName   -> "Schedule"
    MainRoute::class.qualifiedName   -> "Home"
    else -> "Other"
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRythmTheme { AppRoot() }
}
