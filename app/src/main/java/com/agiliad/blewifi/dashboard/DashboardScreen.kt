package com.agiliad.blewifi.dashboard

import android.Manifest
import android.R.attr.onClick
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.agiliad.blewifi.R
import com.agiliad.blewifi.ui.theme.BLEWifiTheme
import com.ble.wifi.connectToWifi

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DashboardScreen(viewModel: SensorViewModel=hiltViewModel(), navigationController: NavController, deviceName: String,  onDisconnect: () -> Unit) {

    var showDisconnectDialog by remember { mutableStateOf(false) }

    val sensorData by viewModel.sensorData.collectAsState()
    val insets = WindowInsets.statusBars.asPaddingValues()

    val context = LocalContext.current
    val ssid: String = "OnePlus 10R 5G"
    val password: String = "e4d66jsw"

    /*val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            connectToWifi(context, ssid, password)
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }*/


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(insets)
        //.padding(16.dp)
    ) {
        val screenWidth = maxWidth
        val controlButtonSize = screenWidth * 0.12f


        // Menu button top-right
        IconButton(
            onClick = {
                     viewModel.downloadLogToDownloads(context)
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            //Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            IconFromPng()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // verticalArrangement = Arrangement.SpaceBetween
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // Top Excavator Image (scaled by screen width)
                Box(
                    modifier = Modifier
                        .size(screenWidth * 0.60f) // 55% of screen width
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.excavatore_circle), // replace with your excavator image
                        contentDescription = "Excavator",
                        modifier = Modifier.fillMaxSize(0.90f),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { showDisconnectDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(48.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.disconnect_btn),
                            contentDescription = "Disconnect",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

               /* Text(
                    // add id here

                    text = "CAT 305 CR Mini Excavator",
                    color = Color.White,
                    fontSize = (screenWidth.value / 18).sp, // adaptive font size

                )*/

                Text(
                    // add id here
                   // text = "CAT 305 CR Mini Excavator",
                    text = deviceName,
                    color = Color.White,
                    fontSize = (screenWidth.value / 18).sp, // adaptive font size
                )

                Spacer(modifier = Modifier.height(20.dp))

               /* // Show connected device name at the top
                Text(
                   // text = "Connected Device: $deviceName",
                    text = "Connected Device: CCVTP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )*/

                // Middle Stats Cards responsive grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),

                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)

                    ) {
                        InfoCard("Engine hours", sensorData?.engine_hours.toString(), R.drawable.engine, Modifier.weight(1f))
                        InfoCard("Battery", sensorData?.battery_voltage.toString(), R.drawable.battery, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoCard(
                            "Oil Pressure",
                            sensorData?.oil_pressure.toString(),
                            R.drawable.oil_pressure,
                            Modifier.weight(1f)
                        )
                        InfoCard(
                            "Temperature",
                            sensorData?.oil_temperature.toString(),
                            R.drawable.temperature,
                            Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

            }
        }
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect") },
            text = { Text("Do you want to disconnect?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.disconnect()
                    onDisconnect()
                    showDisconnectDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onDashboardBackgrounded()
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> viewModel.onDashboardForegrounded()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    imageRes: Int,   // now takes a PNG resource instead of vector
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp),    // keeps proportion
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.card_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            /* Row(
                modifier = Modifier.fillMaxSize()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )*/
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.TopEnd)

                )

                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    horizontalAlignment = Alignment.Start,

                    ) {
                    // Spacer(modifier = Modifier.height(6.dp))
                    Text(text = title, fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                }
            }
        }
    }
}


@Composable
fun ControlButton(
    iconRes: Int,
    selectedIconRes: Int? = null,
    contentDesc: String,
    size: Dp,
    useIconToggle: Boolean = false,
    useOnclick: Boolean = false,
    onClick: () -> Unit = {}
) {
    var isSelected by remember { mutableStateOf(false) }
    val iconToShow = if (useIconToggle && isSelected && selectedIconRes != null) {
        selectedIconRes
    } else {
        iconRes
    }
    Box(
        modifier = Modifier
            .size(size)
            .background(
                if (!useIconToggle && isSelected)
                    Color.Yellow.copy(alpha = 0.2f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable {
                if (useOnclick) onClick()
                else isSelected = !isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconToShow),
            contentDescription = contentDesc,
            tint = if (useIconToggle) Color.White else if (isSelected) Color.Yellow else Color.White,
            modifier = Modifier.fillMaxSize(0.6f)
        )

    }
}

@Composable
fun IconFromPng() {
    val context = LocalContext.current
    val ssid: String = "Agiliad-CDA"
    val password: String = "12345"

    val activity = context as Activity
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //connectToWifi(context, ssid, password)
        } else {
           // Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Image(
        painter = painterResource(id = R.drawable.download),
        contentDescription = "Icon",
        modifier = Modifier.size(24.dp)
        )
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    BLEWifiTheme {
       // DashboardScreen()
    }
}