// app/src/main/java/com/myrythm/AppRoot.kt
package com.myrythm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.auth.navigation.*
import com.auth.viewmodel.AuthViewModel
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppRoot() {

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val routeName = backStack?.destination?.route.orEmpty()

    // JWT 에서 userId 추출
    val ctx = LocalContext.current
    val tokenStore = remember {
        EntryPointAccessors.fromApplication(ctx, CoreEntryPoint::class.java).tokenStore()
    }
    val rawUserId = remember {
        JwtUtils.extractUserId(tokenStore.current().access) ?: ""
    }

    val userId = rawUserId.toLongOrNull()?.toString() ?: "1"

    val authVm: AuthViewModel = hiltViewModel()

    // 로그아웃 Event 감지
    LaunchedEffect(Unit) {
        authVm.events.collectLatest { ev ->
            if (ev == "로그아웃 완료") {
                nav.navigate(LoginRoute) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
        }
    }

    fun isRoute(obj: Any) = routeName == obj::class.qualifiedName
    fun isOf(vararg objs: Any) = objs.any { isRoute(it) }

    val isAuth = isOf(LoginRoute, PwdRoute, SignupRoute)
    val isMain = isRoute(MainRoute)

    val isNews = isRoute(NewsRoute)

    // 숨기기 조건
    val hideTopBar = isAuth || isMain
    val hideBottomBar = isAuth

    fun goHome() = nav.navigate(MainRoute(userId)) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    fun goMyPage() = nav.navigate(MyPageRoute) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    // ⭐ Schedule Flow (무조건 userId 전달)
    fun goScheduleFlow() = nav.navigate(CameraRoute(userId)) {
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
                    currentUserId = userId,                   // ⭐ 전달 필수
                    onScheduleClick = { goScheduleFlow() },    // ⭐ 알약 눌렀을 때
                    onTabSelected = { tab ->
                        when (tab) {
                            "Home"   -> goHome()
                            "MyPage" -> goMyPage()
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
                chatbotNavGraph()
                schedulerNavGraph(nav, userId)
                mypageNavGraph(nav, onLogoutClick = { authVm.logout() })
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
    RegiRoute::class.qualifiedName -> "Schedule"
    MainRoute::class.qualifiedName -> "Home"
    else -> "Other"
}
