package com.homehub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homehub.app.ui.screens.adddevice.AddDeviceScreen
import com.homehub.app.ui.screens.dashboard.DashboardScreen
import com.homehub.app.ui.screens.login.LoginScreen
import com.homehub.app.ui.screens.activity.ActivityFeedScreen
import com.homehub.app.ui.screens.rules.RulesListScreen
import com.homehub.app.ui.screens.rules.CreateRuleScreen

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Dashboard : Destination("dashboard")
    data object AddDevice : Destination("add_device")
    // Phase 4 will still add device detail and rule builder routes here

    data object ActivityFeed : Destination("activity_feed")

    data object RulesList : Destination("rules_list")

    data object CreateRule : Destination("create_rule")
}

@Composable
fun HomeHubNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destination.Login.route) {
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.Dashboard.route) {
                        popUpTo(Destination.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Destination.Dashboard.route) {
            DashboardScreen(
                onAddDevice = { navController.navigate(Destination.AddDevice.route) },
                onViewActivity = { navController.navigate(Destination.ActivityFeed.route) },
                onViewRules = { navController.navigate(Destination.RulesList.route) }
            )
        }
        composable(Destination.RulesList.route) {
            RulesListScreen(
                onBack = { navController.popBackStack() },
                onCreateRule = { navController.navigate(Destination.CreateRule.route) }
            )
        }
        composable(Destination.CreateRule.route) {
            CreateRuleScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destination.AddDevice.route) {
            AddDeviceScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destination.ActivityFeed.route) {
            ActivityFeedScreen(onBack = { navController.popBackStack() })
        }
    }
}