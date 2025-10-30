package com.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.auth.ui.LoginScreen
import com.auth.ui.PwdScreen
import com.auth.ui.SignupScreen
import com.main.navigation.MainRoute

fun NavGraphBuilder.authNavGraph(nav: NavController) {
    navigation<AuthGraph>(startDestination = LoginRoute) {

        composable<LoginRoute> {
            LoginScreen(
                onLogin = { _, _ ->
                    nav.navigate(MainRoute) {
                        popUpTo(AuthGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = { nav.navigate(PwdRoute) },
                onSignUp = { nav.navigate(SignupRoute) }
            )
        }

        composable<PwdRoute> {
            PwdScreen(
                onConfirm = { _, _ ->
                    nav.navigate(LoginRoute) {
                        popUpTo(PwdRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = { nav.navigate(LoginRoute) }
            )
        }

        composable<SignupRoute> {
            SignupScreen(
                onSignupComplete = {
                    nav.navigate(LoginRoute) {
                        popUpTo(SignupRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = { nav.navigate(LoginRoute) }
            )
        }
    }
}

