package com.libraryofmiao.membership.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.libraryofmiao.membership.data.session.SessionManager
import com.libraryofmiao.membership.ui.dashboard.DashboardScreen
import com.libraryofmiao.membership.ui.home.HomeScreen
import com.libraryofmiao.membership.ui.login.LoginScreen
import com.libraryofmiao.membership.ui.membercard.MemberCardScreen
import com.libraryofmiao.membership.ui.register.RegisterScreen
import com.libraryofmiao.membership.ui.verify.VerifyScreen

@Composable
fun LibraryMembershipNavHost(session: SessionManager) {
    val navController = rememberNavController()
    val startDestination = if (session.isLoggedIn()) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(onAuthenticated = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onRegister = { navController.navigate(Screen.Register.createRoute()) },
                onMembers = { navController.navigate(Screen.Dashboard.route) },
                onLogout = {
                    session.clear()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                onEditMember = { id -> navController.navigate(Screen.Register.createRoute(id)) },
                onViewCard = { id -> navController.navigate(Screen.MemberCard.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId")?.takeIf { it.isNotBlank() }
            RegisterScreen(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MemberCard.route,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId") ?: return@composable
            MemberCardScreen(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onVerify = { code -> navController.navigate(Screen.Verify.createRoute(code)) }
            )
        }

        composable(
            route = Screen.Verify.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code")?.takeIf { it.isNotBlank() }
            VerifyScreen(initialCode = code, onBack = { navController.popBackStack() })
        }
    }
}
