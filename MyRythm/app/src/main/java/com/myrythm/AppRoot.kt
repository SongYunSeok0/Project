package com.myrythm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auth.navigation.*
import com.chatbot.navigation.*
import com.design.AppBottomBar
import com.design.AppTopBar
import com.main.navigation.*
import com.map.navigation.*
import com.mypage.navigation.*
import com.news.navigation.*
import com.scheduler.navigation.*

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val routeName = backStack?.destination?.route.orEmpty()

    // 라우트 구분
    fun isRoute(obj: Any) = routeName == obj::class.qualifiedName
    fun isOf(vararg objs: Any) = objs.any { isRoute(it) }

    val isAuth = isOf(LoginRoute, PwdRoute, SignupRoute)
    val isMain = isRoute(MainRoute)
    val isNews = isRoute(NewsRoute)

    val hideTopBar = isAuth || isMain
    val hideBottomBar = isAuth

    // 탭 이동 함수
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
                    },
                    showSearch = isNews, // ✅ 뉴스화면일 때만 검색버튼 표시
                    onSearchClick = {
                        // ✅ NewsScreen에 검색창 표시 신호 전달
                        nav.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("openSearch", true)
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
                newsNavGraph(nav) // ✅ 여기서 NewsScreen 내부에서 검색창 표시됨
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
    MapRoute::class.qualifiedName         -> "지도"
    NewsRoute::class.qualifiedName        -> "뉴스"
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
