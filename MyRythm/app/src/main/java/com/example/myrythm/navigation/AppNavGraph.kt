package com.example.myrythm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.login.LoginScreen
import com.example.login.SignupScreen
import com.example.login.PwdScreen
import com.example.main.MainScreen
import com.example.map.MapScreen
import com.example.mypage.MyPageScreen
import com.example.news.NewsScreen
import com.example.scheduler.SchedulerScreen
import com.example.scheduler.CameraScreen
import com.example.scheduler.OcrScreen
import com.example.scheduler.RegiScreen
import com.example.chatbot.ChatBotScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { _, _ -> navController.navigate(Routes.MAIN) },
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
        composable(Routes.MAIN) { MainScreen() }
        composable(Routes.MAP) { MapScreen() }
        composable(Routes.MYPAGE) { MyPageScreen() }
        composable(Routes.NEWS) { NewsScreen(navController) }
        composable(Routes.SCHEDULER) { SchedulerScreen() }
        composable(Routes.CAMERA) { CameraScreen() }
        composable(Routes.OCR) { OcrScreen() }
        composable(Routes.REGI) { RegiScreen() }
        composable(Routes.CHATBOT) { ChatBotScreen() }
    }
}
