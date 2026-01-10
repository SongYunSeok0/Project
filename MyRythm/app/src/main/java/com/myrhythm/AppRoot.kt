package com.myrhythm

import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
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
import com.auth.viewmodel.LoginViewModel
import com.chatbot.navigation.ChatBotRoute
import com.chatbot.navigation.chatbotNavGraph
import com.data.core.auth.JwtUtils
import com.data.core.di.CoreEntryPoint
import com.domain.repository.*
import com.google.accompanist.swiperefresh.*
import com.healthinsight.navigation.HealthInsightRoute
import com.healthinsight.navigation.healthInsightNavGraph
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

    val loginVm: LoginViewModel = hiltViewModel()
    val stepVm: StepViewModel = hiltViewModel()
    val heartVm: HeartRateViewModel = hiltViewModel()

    val loginUi by loginVm.uiState.collectAsStateWithLifecycle()

    Log.e("AppRoot", "🔄 RECOMPOSE! loginUi.isLoggedIn = ${loginUi.isLoggedIn}")

    val ctx = LocalContext.current
    val tokenStore = EntryPointAccessors
        .fromApplication(ctx, CoreEntryPoint::class.java)
        .tokenStore()

    val access = tokenStore.current().access
    val isLoggedIn = access?.isNotBlank() == true
    val realUserId = JwtUtils.extractUserId(access) ?: "0"
    val userId = loginUi.userId ?: realUserId
    val userIdLong = userId.toLongOrNull() ?: 0L

    Log.e("AppRoot", "========================================")
    Log.e("AppRoot", "loginUi.isLoggedIn: ${loginUi.isLoggedIn}")
    Log.e("AppRoot", "isLoggedIn (from token): $isLoggedIn")
    Log.e("AppRoot", "========================================")

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

    var previousLoginState by remember { mutableStateOf(loginUi.isLoggedIn) }

    // 로그아웃 처리
    LaunchedEffect(loginUi.isLoggedIn) {
        Log.e("AppRoot", "========================================")
        Log.e("AppRoot", "🔥 로그인 상태 변화 감지")
        Log.e("AppRoot", "이전: $previousLoginState → 현재: ${loginUi.isLoggedIn}")
        Log.e("AppRoot", "========================================")

        // 로그인 → 로그아웃으로 변경된 경우만 처리
        if (previousLoginState && !loginUi.isLoggedIn) {
            Log.e("AppRoot", "✅ 로그아웃 감지 - 로그인 화면으로 이동")
            nav.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }

        previousLoginState = loginUi.isLoggedIn
    }

    fun isRoute(k: KClass<*>) =
        routeName.startsWith(k.qualifiedName.orEmpty())

    fun isOf(vararg ks: KClass<*>) = ks.any { isRoute(it) }

    val hideTopBar = isOf(LoginRoute::class, PwdRoute::class, SignupRoute::class) ||
            isRoute(MainRoute::class) ||
            isRoute(UserManageRoute::class) ||
            isRoute(InquiriesManageRoute::class)
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

    val syncEnabled = remember(routeName) {
        isSyncAllowedRoute(routeName)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
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
            swipeEnabled = syncEnabled,
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

                authNavGraph(nav, loginVm)

                mainNavGraph(
                    nav = nav,
                    onLogoutClick = { loginVm.logout() }
                )

                mapNavGraph()
                newsNavGraph(nav, userId)
                schedulerNavGraph(nav)
                mypageNavGraph(
                    nav = nav,
                    heartVm = heartVm,
                    userId = userIdLong,
                    onLogoutClick = {
                        Log.e("AppRoot", "🔥🔥🔥 onLogoutClick 콜백 받음!")
                        loginVm.logout()
                        Log.e("AppRoot", "🔥🔥🔥 loginVm.logout() 호출 완료!")
                    }
                )
                chatbotNavGraph()
                healthInsightNavGraph()
            }
        }
    }
}

private fun isSyncAllowedRoute(routeName: String): Boolean {
    return when {
        routeName.startsWith(MainRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(MyPageRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(EditProfileRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(HeartReportRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(HealthInsightRoute::class.qualifiedName.orEmpty()) -> true
        routeName.startsWith(SchedulerRoute::class.qualifiedName.orEmpty()) -> true
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
    UserManageRoute::class.qualifiedName -> "사용자 관리"
    InquiriesManageRoute::class.qualifiedName -> "문의사항 관리"
    HealthInsightRoute::class.qualifiedName -> "건강 인사이트"
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