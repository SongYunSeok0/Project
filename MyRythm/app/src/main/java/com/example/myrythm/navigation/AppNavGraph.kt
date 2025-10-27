package com.example.myrythm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatbot.ChatBotScreen
import com.example.login.LoginScreen
import com.example.login.PwdScreen
import com.example.login.SignupScreen
import com.example.main.MainScreen
import com.example.map.MapScreen
import com.example.mypage.EditScreen
import com.example.mypage.HeartRateScreen
import com.example.mypage.MyPageScreen
import com.example.news.NewsMainScreen
import com.example.scheduler.CameraScreen
import com.example.scheduler.OcrScreen
import com.example.scheduler.RegiScreen
import com.example.scheduler.SchedulerScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { _, _ ->
                    navController.navigate(Routes.MAIN) {
                        // 로그인 화면 제거하여 뒤로가기로 돌아오지 않게
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = { navController.navigate(Routes.PWD) },
                onSignUp = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSendCode = { /* no-op */ },
                onVerify = { /* no-op */ },
                onComplete = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onWriteLater = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.PWD) { PwdScreen() }

        composable(Routes.MAIN) {
            MainScreen(
                onChatbotClick   = { navController.navigate(Routes.CHATBOT) },
                onSchedulerClick = { navController.navigate(Routes.SCHEDULER) },
                onHeartClick     = { navController.navigate(Routes.HEART) },
                onMapClick       = { navController.navigate(Routes.MAP) },
                onNewsClick      = { navController.navigate(Routes.NEWS) },
            )
        }

        composable(Routes.MAP) {
            MapScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.MYPAGE) {
            MyPageScreen(
                onEditClick = { navController.navigate(Routes.EDIT) },
                onHeartClick = { navController.navigate(Routes.HEART) },
                onLogoutClick = {
                    // 전체 스택을 시작지점(LOGIN)까지 비우고, LOGIN만 남김
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.HEART) { HeartRateScreen() }

        composable(Routes.EDIT) {
            EditScreen(
                onDone = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NEWS) {
            NewsMainScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SCHEDULER) { SchedulerScreen() }

        composable(Routes.CAMERA) {
            CameraScreen(
                onTakePhoto = { navController.navigate(Routes.OCR) },
                onPickFromGallery = { navController.navigate(Routes.OCR) }
            )
        }

        composable(Routes.OCR) {
            OcrScreen(
                onConfirm = { navController.navigate(Routes.REGI) },
                onRetake  = { navController.navigate(Routes.CAMERA) }
            )
        }

        composable(Routes.REGI) {
            RegiScreen(
                onSubmit = {
                    navController.navigate(Routes.SCHEDULER)
                }
            )
        }

        composable(Routes.CHATBOT) { ChatBotScreen() }
    }
}
