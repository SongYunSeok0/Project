package com.myrythm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.core.auth.JwtUtils
import com.core.di.CoreEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val routeName = backStack?.destination?.route.orEmpty()

    // TokenStore 주입 → JWT에서 userId 추출
    val ctx = LocalContext.current
    val tokenStore = remember {
        EntryPointAccessors.fromApplication(ctx, CoreEntryPoint::class.java).tokenStore()
    }
    val userId = remember {
        JwtUtils.extractUserId(tokenStore.current().access) ?: ""
    }

    fun isRoute(obj: Any) = routeName == obj::class.qualifiedName
    fun isOf(vararg objs: Any) = objs.any { isRoute(it) }

    val isAuth = isOf(LoginRoute, PwdRoute, SignupRoute)
    val isMain = isRoute(MainRoute)
    val isNews = isRoute(NewsRoute)

    val hideTopBar = isAuth || isMain
    val hideBottomBar = isAuth

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
                        if (nav.previousBackStackEntry != null) nav.popBackStack() else goHome()
                    },
                    showSearch = isNews,
                    onSearchClick = {
                        nav.currentBackStackEntry?.savedStateHandle?.set("openSearch", true)
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
                schedulerNavGraph(nav, userId) // ← userId 전달
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
