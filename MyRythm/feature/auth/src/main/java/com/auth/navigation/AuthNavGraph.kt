/*
package com.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.auth.ui.LoginScreen
import com.auth.ui.PwdScreen
import com.auth.ui.SignupScreen
import com.shared.navigation.MainRoute

fun NavGraphBuilder.authNavGraph(nav: NavController) {
    navigation<AuthGraph>(startDestination = LoginRoute) {

        composable<LoginRoute> {
            LoginScreen(
                onLogin = { userId, _ ->
                    nav.navigate(MainRoute(userId = userId)) {
                        popUpTo(AuthGraph) { inclusive = true }
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

        */
/*1124 주석 composable<SignupRoute> {
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
        }*//*

        // 1124 수정authNavGraph.kt
        composable<SignupRoute> {
            val args = it.toRoute<SignupRoute>()
            SignupScreen(
                socialId = args.socialId,
                provider = args.provider,
                onSignupComplete = {
                    // 소셜 회원가입인 경우 socialId 사용
                    if (args.socialId != null && args.provider != null) {
                        nav.navigate(MainRoute(userId = args.socialId)) {
                            popUpTo<AuthGraph> { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // 일반 회원가입은 로그인 화면으로
                        nav.navigate(LoginRoute) {
                            popUpTo<AuthGraph> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onBackToLogin = { nav.navigate(LoginRoute) }
            )
        }
    }
}

*/
package com.auth.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.auth.ui.LoginScreen
import com.auth.ui.PwdScreen
import com.auth.ui.SignupScreen
import com.shared.navigation.MainRoute

fun NavGraphBuilder.authNavGraph(nav: NavController) {
    navigation<AuthGraph>(startDestination = LoginRoute) {

        composable<LoginRoute> {
            Log.e("AuthNavGraph", "🏗️ LoginRoute Composable")
            LoginScreen(
                onLogin = { userId, password ->
                    Log.e("AuthNavGraph", "🚢 ========== onLogin 호출됨 ==========")
                    Log.e("AuthNavGraph", "🚢 userId = $userId")
                    Log.e("AuthNavGraph", "🚢 password = ${password.take(3)}...")

                    nav.navigate(MainRoute(userId = userId)) {
                        popUpTo(AuthGraph) { inclusive = true }
                        launchSingleTop = true
                    }

                    Log.e("AuthNavGraph", "🚢 navigate 호출 완료")
                },
                onForgotPassword = { nav.navigate(PwdRoute) },
                onSignUp = { nav.navigate(SignupRoute()) },
                onSocialSignUp = { socialId, provider ->
                    Log.e("AuthNavGraph", "👤 onSocialSignUp: $socialId, $provider")
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
                    // 소셜 회원가입인 경우 socialId 사용
                    if (args.socialId != null && args.provider != null) {
                        Log.e("AuthNavGraph", "📝 소셜 회원가입 완료 → MainRoute")
                        nav.navigate(MainRoute(userId = args.socialId)) {
                            popUpTo<AuthGraph> { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // 일반 회원가입은 로그인 화면으로
                        Log.e("AuthNavGraph", "📝 일반 회원가입 완료 → LoginRoute")
                        nav.navigate(LoginRoute) {
                            popUpTo<AuthGraph> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onBackToLogin = { nav.navigate(LoginRoute) }
            )
        }
    }
}