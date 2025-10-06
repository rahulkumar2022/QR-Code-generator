package com.synergeticsciences.qrcodegenerator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.synergeticsciences.qrcodegenerator.ui.screen.*

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onRequestCameraPermission: (() -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = { navController.navigate("home") }
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                onRequestCameraPermission = onRequestCameraPermission
            )
        }

        composable("history") {
            HistoryScreen(navController = navController)
        }

        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}
