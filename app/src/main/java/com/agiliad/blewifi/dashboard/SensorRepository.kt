package com.agiliad.blewifi.dashboard

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SensorRepository @Inject constructor(private val api: ApiService) {
    suspend fun fetchAllSensorData(): SensorData {
        val engine = api.getEngineHours().body()?.get("engine_hours") ?: 0.0
        val fuel = api.getFuelLevel().body()?.get("fuel_level") ?: 0.0
        val battery = api.getBatteryVoltage().body()?.get("battery_voltage") ?: 0.0
        val pressure = api.getOilPressure().body()?.get("oil_pressure") ?: 0.0
        val temperature = api.getOilTemperature().body()?.get("oil_temperature") ?: 0.0

        return SensorData(engine, fuel, battery, pressure, temperature)
    }
}
