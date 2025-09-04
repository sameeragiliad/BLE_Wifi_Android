package com.agiliad.blewifi.dashboard


import dagger.Provides
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response

interface ApiService  {
    @GET("api/engine-hours")
    suspend fun getEngineHours(): Response<Map<String, Double>>

    @GET("api/fuel-level")
    suspend fun getFuelLevel(): Response<Map<String, Double>>


    @GET("api/battery-voltage")
    suspend fun getBatteryVoltage(): Response<Map<String, Double>>


    @GET("api/oil-pressure")
    suspend fun getOilPressure(): Response<Map<String, Double>>


    @GET("api/oil-temperature")
    suspend fun getOilTemperature(): Response<Map<String, Double>>
}
