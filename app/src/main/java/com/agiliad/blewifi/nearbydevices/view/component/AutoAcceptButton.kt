package com.agiliad.blewifi.nearbydevices.view.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AutoAcceptButtons(
    modifier: Modifier,
    acceptDuration: Int = 2000,
    onAccept: () -> Unit,
    onMoreDevices: ()->Unit){

    var progress by remember  { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val interval = 50L
        val steps = acceptDuration / interval
        repeat(steps.toInt()){
            delay(interval)
            progress += 1f/steps
        }
        onAccept()
    }

    Row (modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween){

       /* OutlinedButton (
            onClick = onMoreDevices,
            modifier = Modifier.weight(1f).padding(end = 8.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDE21)),
            border = BorderStroke(width = 0.dp, color =Color (0x00FFFFFF))

        ) {
            Text( text = "More Machines",
                color = Color.Black,
                fontWeight = FontWeight.Bold)
        }*/
        Box( modifier = Modifier.weight(1f)
            .padding(start = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(color = Color(0x33FFDE21))
        ){
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color(0xFFFFDE21)) // green progress
            )
            Text(
                text = "More Machines",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

}