package com.library.membership

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.library.membership.ui.AdminListScreen
import com.library.membership.ui.MemberCardScreen
import com.library.membership.ui.MemberDetailScreen
import com.library.membership.ui.RegisterScreen
import com.library.membership.ui.RoleSelectScreen
import com.library.membership.ui.VerifyScreen
import com.library.membership.ui.theme.LibraryMembershipTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Backend URL is already set in ApiClient.kt (library-membership-system.libraryofmiao.workers.dev)

        setContent {
            LibraryMembershipTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "role") {
        composable("role") {
            RoleSelectScreen(
                onAdmin = { navController.navigate("admin_list") },
                onVerify = { navController.navigate("verify") },
                onMemberCard = { navController.navigate("member_card") }
            )
        }
        composable("admin_list") {
            AdminListScreen(
                onBack = { navController.popBackStack() },
                onAddNew = { navController.navigate("register") },
                onOpenMember = { memberId -> navController.navigate("member_detail/$memberId") }
            )
        }
        composable(
            "member_detail/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
            MemberDetailScreen(memberId = memberId, onBack = { navController.popBackStack() })
        }
        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = { memberId ->
                    navController.popBackStack("admin_list", inclusive = false)
                    navController.navigate("member_detail/$memberId")
                }
            )
        }
        composable("verify") {
            VerifyScreen(onBack = { navController.popBackStack() })
        }
        composable("member_card") {
            MemberCardScreen(onBack = { navController.popBackStack() })
        }
    }
}
