package com.agiliad.blewifi.nearbydevices.view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agiliad.blewifi.nearbydevices.model.Device

@Composable
fun DeviceCard(device: Device, modifier: Modifier = Modifier){

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ){
        Row(modifier = Modifier.padding(16.dp)){
         Column (modifier = Modifier.weight(1f)) {
             Text(text = device.name, style = MaterialTheme.typography.titleMedium)
         }
        }
    }
}