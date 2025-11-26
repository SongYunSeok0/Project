package com.myrythm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auth.navigation.*
import com.auth.viewmodel.AuthViewModel
import com.chatbot.navigation.*
import com.shared.bar.AppBottomBar
import com.shared.bar.AppTopBar
import com.shared.navigation.*
import com.map.navigation.*
import com.mypage.navigation.*
import com.news.navigation.*
import com.scheduler.navigation.*
import com.data.core.auth.JwtUtils
import com.data.core.di.CoreEntryPoint
import com.myrhythm.navigation.mainNavGraph
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest
import kotlin.reflect.KClass
import com.myrhythm.health.StepViewModel

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val routeName = backStack?.destination?.route.orEmpty()

    // TokenStore → JWT userId 추출
    val ctx = LocalContext.current
    val tokenStore = remember {
        EntryPointAccessors.fromApplication(ctx, CoreEntryPoint::class.java).tokenStore()
    }
    val userId = remember {
        JwtUtils.extractUserId(tokenStore.current().access) ?: ""
    }

    // AuthViewModel
    val authVm: AuthViewModel = hiltViewModel()

    // 🔥 StepViewModel을 AppRoot에서 단 1개 생성
    val stepVm: StepViewModel = hiltViewModel()

    // 🔥 앱 시작 시 단 1회만 실행
    LaunchedEffect(Unit) {
        stepVm.checkPermission()
        stepVm.startAutoUpdateOnce()
    }

    // 로그아웃 수신
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

    fun isRoute(k: KClass<*>) =
        routeName.startsWith(k.qualifiedName.orEmpty())

    fun isOf(vararg ks: KClass<*>) = ks.any { isRoute(it) }

    val isAuth = isOf(LoginRoute::class, PwdRoute::class, SignupRoute::class)
    val isMain = isRoute(MainRoute::class)
    val isNews = isRoute(NewsRoute::class)
    val isChat = isRoute(ChatBotRoute::class)

    val hideTopBar = isAuth || isMain
    val hideBottomBar = isAuth || isChat

    // 탭 이동
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
            NavHost(nav, startDestination = AuthGraph) {
                authNavGraph(nav)
                mainNavGraph(nav)              // ← userId는 Route 내부에서 decode
                mapNavGraph()
                newsNavGraph(nav, userId)
                schedulerNavGraph(nav)
                mypageNavGraph(nav, onLogoutClick = { authVm.logout() })
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

private fun tabFor(routeName: String) = when {
    routeName.startsWith(MyPageRoute::class.qualifiedName.orEmpty()) -> "MyPage"
    routeName.startsWith(SchedulerRoute::class.qualifiedName.orEmpty()) -> "Schedule"
    routeName.startsWith(CameraRoute::class.qualifiedName.orEmpty()) -> "Schedule"
    routeName.startsWith(OcrRoute::class.qualifiedName.orEmpty()) -> "Schedule"
    routeName.startsWith(RegiRoute::class.qualifiedName.orEmpty()) -> "Schedule"
    routeName.startsWith(MainRoute::class.qualifiedName.orEmpty()) -> "Home"
    else -> "Other"
}
