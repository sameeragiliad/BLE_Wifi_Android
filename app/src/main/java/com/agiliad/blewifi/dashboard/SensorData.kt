package com.agiliad.blewifi.dashboard


data class SensorData(
    val engine_hours: Double,
    val fuel_level: Double,
    val battery_voltage: Double,
    val oil_pressure: Double,
    val oil_temperature: Double
)
