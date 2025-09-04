package com.agiliad.blewifi.nearbydevices.model

data class Device(
    val id: String,
    val name:String,
    val signalStrength: Int,
    val type: String,
    val mac: String,
    val isFavorite: Boolean = false,
    val isPreviouslyConnected: Boolean = false,
    val connectionState: String = "Connecting"
)