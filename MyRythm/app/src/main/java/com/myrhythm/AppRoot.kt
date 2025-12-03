package com.myrhythm

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auth.navigation.*
import com.auth.viewmodel.AuthViewModel
import com.chatbot.navigation.ChatBotRoute
import com.chatbot.navigation.chatbotNavGraph
import com.data.core.auth.JwtUtils
import com.data.core.di.CoreEntryPoint
import com.domain.repository.*
import com.google.accompanist.swiperefresh.*
import com.map.navigation.MapRoute
import com.map.navigation.mapNavGraph
import com.mypage.navigation.*
import com.myrhythm.navigation.mainNavGraph
import com.myrhythm.viewmodel.HeartRateViewModel
import com.myrhythm.viewmodel.StepViewModel
import com.news.navigation.NewsRoute
import com.news.navigation.newsNavGraph
import com.scheduler.navigation.*
import com.shared.bar.AppBottomBar
import com.shared.bar.AppTopBar
import com.shared.navigation.MainRoute
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface SyncEntryPoint {
    fun regiRepository(): RegiRepository
    fun planRepository(): PlanRepository
    fun heartRepository(): HeartRateRepository
    fun userRepository(): UserRepository
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(startFromLogin: Boolean = false) {

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val routeName = backStack?.destination?.route.orEmpty()

    val authVm: AuthViewModel = hiltViewModel()
    val stepVm: StepViewModel = hiltViewModel()
    val heartVm: HeartRateViewModel = hiltViewModel()

    val ui by authVm.state.collectAsStateWithLifecycle()

    val ctx = LocalContext.current
    val tokenStore = EntryPointAccessors
        .fromApplication(ctx, CoreEntryPoint::class.java)
        .tokenStore()

    val access = tokenStore.current().access
    val isLoggedIn = access?.isNotBlank() == true
    val realUserId = JwtUtils.extractUserId(access) ?: "0"
    val userId = ui.userId ?: realUserId
    val userIdLong = userId.toLongOrNull() ?: 0L

    val startDestination =
        if (!isLoggedIn || startFromLogin) AuthGraph
        else MainRoute(realUserId)

    LaunchedEffect(Unit) {
        stepVm.checkPermission()
        stepVm.startAutoUpdateOnce()
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            heartVm.start()
        }
    }

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

    val hideTopBar = isOf(LoginRoute::class, PwdRoute::class, SignupRoute::class) ||
            isRoute(MainRoute::class)
    val hideBottomBar = isOf(LoginRoute::class, PwdRoute::class, SignupRoute::class) ||
            isRoute(ChatBotRoute::class)

    fun goHome() = nav.navigate(MainRoute(userId)) {
        popUpTo(0); launchSingleTop = true
    }
    fun goMyPage() = nav.navigate(MyPageRoute) {
        popUpTo(0); launchSingleTop = true
    }
    fun goScheduleFlow() = nav.navigate(CameraRoute(userId)) {
        popUpTo(0); launchSingleTop = true
    }

    val syncEntry = EntryPointAccessors.fromApplication(ctx, SyncEntryPoint::class.java)
    val regiRepo = syncEntry.regiRepository()
    val planRepo = syncEntry.planRepository()
    val heartRepo = syncEntry.heartRepository()
    val userRepo = syncEntry.userRepository()

    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refreshAll() {
        scope.launch {
            refreshing = true
            regiRepo.syncRegiHistories(userIdLong)
            planRepo.syncPlans(userIdLong)
            heartRepo.syncHeartHistory()
            userRepo.syncUser()
            Log.d("Sync", "싱크완료")
            refreshing = false
        }
    }

    // 🔹 현재 route 기준으로 싱크 허용 여부 결정
    val syncEnabled = remember(routeName) {
        isSyncAllowedRoute(routeName)
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
                    showSearch = isRoute(NewsRoute::class),
                    onSearchClick = {
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
                            "Home" -> goHome()
                            "MyPage" -> goMyPage()
                            "Schedule" -> goScheduleFlow()
                        }
                    }
                )
            }
        }
    ) { inner ->

        SwipeRefresh(
            modifier = Modifier.padding(inner),
            state = rememberSwipeRefreshState(isRefreshing = refreshing),
            swipeEnabled = syncEnabled,              // 🔹 허용된 화면에서만 제스처 활성화
            onRefresh = {
                if (syncEnabled) {
                    refreshAll()
                } else {
                    Log.d("Sync", "이 화면에서는 싱크 비활성")
                }
            }
        ) {
            NavHost(
                navController = nav,
                startDestination = startDestination
            ) {

                // 항상 전체 그래프 등록
                authNavGraph(nav)

                mainNavGraph(
                    nav = nav,
                    onLogoutClick = { authVm.logout() }
                )

                mapNavGraph()
                newsNavGraph(nav, userId)
                schedulerNavGraph(nav)
                mypageNavGraph(
                    nav = nav,
                    heartVm = heartVm,
                    userId = userIdLong,
                    onLogoutClick = { authVm.logout() }
                )
                chatbotNavGraph()
            }
        }
    }
}


private fun isSyncAllowedRoute(routeName: String): Boolean {
    return when {
        // 홈
        routeName.startsWith(MainRoute::class.qualifiedName.orEmpty()) -> true

        // 마이페이지 및 관련 화면
        routeName.startsWith(MyPageRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(EditProfileRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(HeartReportRoute::class.qualifiedName.orEmpty()) -> true

        // 일정 / 처방 관련
        routeName.startsWith(SchedulerRoute::class.qualifiedName.orEmpty()) -> true

        // 뉴스
        routeName.startsWith(NewsRoute::class.qualifiedName.orEmpty()) -> true

        else -> false
    }
}

private fun titleFor(routeName: String) = when (routeName) {
    MyPageRoute::class.qualifiedName -> "마이페이지"
    SchedulerRoute::class.qualifiedName -> "일정"
    RegiRoute::class.qualifiedName -> "처방전 등록"
    CameraRoute::class.qualifiedName -> "카메라"
    OcrRoute::class.qualifiedName -> "처방전 인식"
    HeartReportRoute::class.qualifiedName -> "심박수"
    EditProfileRoute::class.qualifiedName -> "내 정보 수정"
    ChatBotRoute::class.qualifiedName -> "챗봇"
    MapRoute::class.qualifiedName -> "지도"
    NewsRoute::class.qualifiedName -> "뉴스"
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
