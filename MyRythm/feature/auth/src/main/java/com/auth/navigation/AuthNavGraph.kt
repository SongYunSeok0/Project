package com.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.auth.ui.LoginScreen
import com.auth.ui.PwdScreen
import com.auth.ui.SignupScreen
import com.shared.navigation.MainRoute

fun NavGraphBuilder.authNavGraph(nav: NavController) {
    navigation<AuthGraph>(startDestination = LoginRoute) {

        composable<LoginRoute> {
            LoginScreen(
                onLogin = { userId, password ->
                    nav.navigate(MainRoute(userId = userId)) {
                        popUpTo(AuthGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = { nav.navigate(PwdRoute) },
                onSignUp = { nav.navigate(SignupRoute) },
            )
        }

        // 1201 수정
        composable<PwdRoute> {
            PwdScreen(
                onBackToLogin = {
                    nav.navigate(LoginRoute) {
                        popUpTo<PwdRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<SignupRoute> {
            SignupScreen(
                onSignupComplete = {
                    nav.navigate(LoginRoute) {
                        popUpTo<AuthGraph> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = { nav.navigate(LoginRoute) }
            )
        }
    }
}