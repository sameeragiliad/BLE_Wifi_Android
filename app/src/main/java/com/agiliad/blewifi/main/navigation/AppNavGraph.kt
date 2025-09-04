package com.agiliad.blewifi.main.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agiliad.blewifi.main.view.SplashScreen
import com.agiliad.blewifi.nearbydevices.view.NearbyScreen
import com.agiliad.blewifi.nearbydevices.view.PreferredDeviceConnectScreen

@Composable
fun AppNavGraph(){
    val navController = rememberNavController()

    NavHost(navController, startDestination = "splashscreen") {
        composable("splashscreen") {
            SplashScreen() {
                navController.navigate("NearbyDevices")
            }
        }
        composable("NearbyDevices") {
            NearbyScreen()
            /*PreferredDeviceConnectScreen {
                navController.navigate("Dashboard")
            }*/
        }
        composable("Dashboard") {
            /*AssetControlScreen {
               // navController.navigate("NearbyDevices")
            }*/
        }
    }
}