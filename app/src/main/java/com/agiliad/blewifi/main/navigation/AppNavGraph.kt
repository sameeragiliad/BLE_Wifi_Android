package com.agiliad.blewifi.main.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agiliad.blewifi.dashboard.DashboardScreen
import com.agiliad.blewifi.main.view.SplashScreen
import com.agiliad.blewifi.nearbydevices.view.NearbyScreen


@Composable
fun AppNavGraph(){
    val navController = rememberNavController()

    NavHost(navController, startDestination = "NearbyDevices") {
   // NavHost(navController, startDestination = "Dashboard/{deviceName}") {
        composable("splashscreen") {
            SplashScreen() {
                navController.navigate("NearbyDevices")
            }
        }
        composable("NearbyDevices") {
           NearbyScreen(navigationController = navController) { deviceName ->
                navController.navigate("Dashboard/$deviceName")
            }
        }
        composable("Dashboard/{deviceName}") { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: ""
            DashboardScreen(navigationController=navController, deviceName = deviceName, onDisconnect = {
                navController.navigate("NearbyDevices")
            })
        }
    }
}