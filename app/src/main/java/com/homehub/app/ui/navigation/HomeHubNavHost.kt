package com.homehub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homehub.app.ui.screens.dashboard.DashboardScreen
import com.homehub.app.ui.screens.login.LoginScreen

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Dashboard : Destination("dashboard")
    // Phase 4 will add device detail, add-device, and rule builder routes here
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
            DashboardScreen()
        }
    }
}