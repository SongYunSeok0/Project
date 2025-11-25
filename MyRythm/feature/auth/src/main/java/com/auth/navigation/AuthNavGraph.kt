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
                /*1124
                onSocialSignUp = { socialId, provider ->
                    Log.e("AuthNavGraph", "👤 onSocialSignUp: $socialId, $provider")
                    nav.navigate(SignupRoute(socialId = socialId, provider = provider))
                }*/
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

        // 1124 로컬 회원가입만 처리 (socialId, provider 제거)
        composable<SignupRoute> {
            SignupScreen(
                onSignupComplete = {
                    // 로컬 회원가입 완료 → 로그인 화면으로
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