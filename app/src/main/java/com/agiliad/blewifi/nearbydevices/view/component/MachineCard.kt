package com.agiliad.blewifi.nearbydevices.view.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agiliad.blewifi.R

@Composable
fun MachineCard(machineName: String , onOtherClick : ()-> Unit, onAccept : ()-> Unit){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
    Card (modifier =  Modifier.align(Alignment.BottomCenter)
        .padding(start = 16.dp, end = 16.dp, bottom =  48.dp)
        .height(400.dp)
        .fillMaxWidth()

        .clip(RoundedCornerShape(24.dp))
        .shadow(  elevation = 24.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.25f),
            spotColor = Color.Black.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),

//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    )
    {
        Column (modifier = Modifier.fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(R.drawable.mini_excavetor),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(291.dp).fillMaxWidth()
            )


            Text(text = machineName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.SansSerif,color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
//            Text(
//                text = "Look for other machines",
//                fontSize = 20.sp,
//                color = Color(0xFF3366CC),
//                fontFamily = FontFamily.SansSerif,
//                textDecoration = TextDecoration.Underline,
//                fontWeight = FontWeight.SemiBold,
//                modifier = Modifier.clickable { onOtherClick() }
//            )
            AutoAcceptButtons(modifier = Modifier, onAccept = {onAccept()}, onMoreDevices = {onOtherClick()})


        }
    }
}
}