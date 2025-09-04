package com.agiliad.blewifi.nearbydevices.view.component

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agiliad.blewifi.nearbydevices.model.Device

@Composable
fun DevicePopupModal (devices: List<Device>, onDismiss: () -> Unit){
    Box(
        modifier = Modifier.fillMaxSize()
            .clickable (onClick = onDismiss)
    )
    {
        Card (modifier = Modifier.align(Alignment.BottomCenter)
            .padding(24.dp)
            .wrapContentHeight(),

            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
            ){


            Column(modifier = Modifier.padding(16.dp)){
                Text("Nearby..",style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                devices.forEach { device ->
                    DeviceCard(device = device)
                }
            }
        }
    }
}