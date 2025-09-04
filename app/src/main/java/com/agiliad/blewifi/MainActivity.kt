package com.agiliad.blewifi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.agiliad.blewifi.ui.theme.BLEWifiTheme
import android.annotation.SuppressLint
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BLEWifiTheme {
                ExcavatorDashboardScreen()
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ExcavatorDashboardScreen() {
    val insets = WindowInsets.statusBars.asPaddingValues()
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
            onClick = { },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
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
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "CAT 305 CR Mini Excavator",
                    color = Color.White,
                    fontSize = (screenWidth.value / 18).sp, // adaptive font size

                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        InfoCard("Engine hours", "1435 hrs", R.drawable.engine, Modifier.weight(1f))
                        InfoCard("Battery", "25.1 V", R.drawable.battery, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoCard(
                            "Oil Pressure",
                            "10.4 psi",
                            R.drawable.oil_pressure,
                            Modifier.weight(1f)
                        )
                        InfoCard(
                            "Temperature",
                            "78 °C",
                            R.drawable.temperature,
                            Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Menu Card responsive
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bottom_menu_bg),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillWidth
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,

                            ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ControlButton(
                                    R.drawable.lock,
                                    R.drawable.unlock,
                                    "Lock",
                                    size = controlButtonSize,
                                    useIconToggle = true
                                )
                                ControlButton(
                                    R.drawable.light,
                                    contentDesc = "Light",
                                    size = controlButtonSize
                                )
                                ControlButton(
                                    R.drawable.horn,
                                    contentDesc = "Sound",
                                    size = controlButtonSize
                                )
                                ControlButton(
                                    R.drawable.fan,
                                    contentDesc = "Fan",
                                    size = controlButtonSize,
                                    useOnclick = true
                                ) {
                                    /*showTemperaturePopup = true*/
                                }
                            }
                        }
                    }

                }
            }
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

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    BLEWifiTheme {
        ExcavatorDashboardScreen()
    }
}