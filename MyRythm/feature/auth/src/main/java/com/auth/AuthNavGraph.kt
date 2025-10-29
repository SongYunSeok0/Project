// feature/auth/src/main/java/com/auth/AuthNavGraph.kt
package com.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.navigation.*

fun NavGraphBuilder.authNavGraph(nav: NavController) {
    navigation<AuthGraph>(startDestination = LoginRoute) {

        composable<LoginRoute> {
            LoginScreen(
                onLogin = { id, pw ->
                    // TODO 실제 로그인 처리 후 성공 시 이동
                    nav.navigate(MainRoute) {
                        popUpTo(AuthGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = { nav.navigate(PwdRoute) },
                onSignUp = { nav.navigate(SignupRoute) }
            )
        }

        // PwdScreen/SignupScreen이 파라미터 없으므로 그대로 호출
        composable<PwdRoute>   { PwdScreen() }
        composable<SignupRoute>{ SignupScreen() }
    }
}
