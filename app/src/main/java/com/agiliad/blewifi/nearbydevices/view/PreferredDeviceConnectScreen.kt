package com.agiliad.blewifi.nearbydevices.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.agiliad.blewifi.R
import com.agiliad.blewifi.nearbydevices.view.component.MachineCard
import com.agiliad.blewifi.nearbydevices.viewmodel.NearbyDevicesViewModel

@Composable
fun PreferredDeviceConnectScreen(viewModel: NearbyDevicesViewModel = hiltViewModel(), onDeviceConnected: () -> Unit) {
    //val
    var isListDisplayed = remember { false }

    val devices by viewModel.devices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    LaunchedEffect(Unit) {
        // BLE scan is started in ViewModel init
    }
    LaunchedEffect(connectionState) {
        if (connectionState == com.ble.model.ConnectionState.CONNECTED) {
            android.util.Log.d("PreferredDeviceConnectScreen", "onDeviceConnected")
            onDeviceConnected()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().background(color = Color(0x7F000000)),
            contentScale = ContentScale.Fit
        )
        if (devices.isNotEmpty()) {
            AnimatedVisibility(visible = !isListDisplayed,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(400)
                )
            ) {
                MachineCard(devices[0].name, onAccept = { viewModel.connectToDevice(devices[0].mac) }, onOtherClick = { isListDisplayed = true })
            }

        }


    }

}