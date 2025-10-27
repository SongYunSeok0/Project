package com.example.myrythm

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
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
    val activity = LocalContext.current as? Activity   // 안전 캐스팅

    Scaffold(
        topBar = {
            AppTopBar(
                title = titleFor(route),
                onBackClick = {
                    if (!nav.popBackStack()) activity?.finish()
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = tabFor(route),
                onTabSelected = { tab ->
                    when (tab) {
                        "Home" -> {
                            nav.navigate(Routes.MAIN) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        "MyPage" -> {
                            nav.navigate(Routes.MYPAGE) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        "Schedule" -> {
                            nav.navigate(Routes.CAMERA) { launchSingleTop = true }
                        }
                    }
                }
            )
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            AppNavGraph(navController = nav)
        }
    }
}

private fun titleFor(route: String) = when (route) {
    Routes.MYPAGE -> "마이페이지"
    Routes.MAP -> "내 정보 수정"
    Routes.SCHEDULER -> "심박수"
    Routes.NEWS -> "뉴스"
    Routes.CHATBOT -> "챗봇"
    Routes.LOGIN -> "로그인"
    Routes.CAMERA -> "카메라"
    else -> "마이 리듬"
}

private fun tabFor(route: String) = when (route) {
    Routes.MYPAGE -> "MyPage"
    Routes.SCHEDULER -> "Schedule"
    else -> "Home"
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRythmTheme { AppRoot() }
}
