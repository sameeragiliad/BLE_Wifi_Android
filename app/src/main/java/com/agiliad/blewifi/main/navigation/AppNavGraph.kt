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
        composable("splashscreen") {
            SplashScreen() {
                navController.navigate("NearbyDevices")
            }
        }
        composable("NearbyDevices") {
           // NearbyScreen(navigationController=navController)
            NearbyScreen {
                navController.navigate("Dashboard")
            }
        }
        composable("Dashboard") {
            DashboardScreen(navigationController=navController)
            /*AssetControlScreen {
               // navController.navigate("NearbyDevices")
            }*/
        }
    }
}