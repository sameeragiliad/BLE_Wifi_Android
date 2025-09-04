package com.agiliad.blewifi.nearbydevices.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.agiliad.blewifi.nearbydevices.viewmodel.NearbyDevicesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(viewModel: NearbyDevicesViewModel = hiltViewModel(), onConnect: () -> Unit) {

    var isListDisplayed = remember { false }

    val devices by viewModel.devices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    if (connectionState ==  com.ble.model.ConnectionState.CONNECTED) {
        onConnect()
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val imageHeight = screenHeight * 0.5f
    val density = LocalDensity.current
    val startYPx = with(density) { (imageHeight * 0.7f).toPx() }
    val endYPx = with(density) { imageHeight.toPx() }
    val favouriteItems = remember { mutableStateListOf<String>() }
  /*  var items = listOf(
        "Item 1 details",
        "Motor Grader",
        "CAT Hydraulic Excavator",
        "Truck Loader",
        "Bulldozer",
        "Wheel Loader",
        "Backhoe Loader",
        "Skid Steer Loader"
    )*/
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val insets = WindowInsets.statusBars.asPaddingValues()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(insets)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart)
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Text(
                text = "Nearby",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(Color(0xFFFFC107), shape = RoundedCornerShape(2.dp))
            )
        }
        // Spacer(modifier = Modifier.height(24.dp))

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    delay(1500)
                  //  items = items.shuffled()

                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
                .padding(top = 70.dp)
        ) {
            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                itemsIndexed(devices) { index, item ->

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                           // .clickable { navigationController.navigate("Dashboard") } //viewModel.connectToDevice(devices[0].mac)
                            .clickable {
                                viewModel.connectToDevice(devices[index])
                            }

                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {

                            Text(
                                text = item.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,

                                )
                            Text(
                                text = item.connectionState,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,

                                )

                            if (index != devices.lastIndex) {
                                CustomFadedDivider()
                                // Divider(color = Color.White.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }

    }
}


@Composable
fun CustomFadedDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp) // Base height of divider
    ) {
        val gradient = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0f),  // transparent at start
                Color.White.copy(alpha = 0.4f), // more visible in middle
                Color.White.copy(alpha = 0f)   // transparent at end
            )
        )
        drawRect(brush = gradient, size = size)
    }
}



@Preview
@Composable
fun PreviewNearbyScreen() {
   // NearbyScreen()
}