package com.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
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
                        popUpTo<AuthGraph> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = { nav.navigate(PwdRoute) },
                onSignUp = { nav.navigate(SignupRoute()) },
                onSocialSignUp = { socialId, provider ->
                    nav.navigate(SignupRoute(socialId = socialId, provider = provider))
                }
            )
        }

        composable<PwdRoute> {
            PwdScreen(
                onConfirm = { _, _ ->
                    nav.navigate(LoginRoute) {
                        popUpTo<PwdRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = { nav.navigate(LoginRoute) }
            )
        }

        composable<SignupRoute> {
            val args = it.toRoute<SignupRoute>()
            SignupScreen(
                socialId = args.socialId,
                provider = args.provider,
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
