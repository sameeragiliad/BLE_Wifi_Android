package com.agiliad.blewifi.dashboard

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
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

    suspend fun downloadDiagnosticsToDownloads(context: Context): Boolean {
        return try {
            val response = api.downloadDiagnostics()
            if (response.isSuccessful) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "diagnostics.log")
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, contentValues) ?: return false

                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    response.body()?.byteStream()?.copyTo(outputStream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)

                NotificationHelper.showDownloadNotification(context, true, itemUri)
                true
            } else {
                NotificationHelper.showDownloadNotification(context, false, null)
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationHelper.showDownloadNotification(context, false, null)
            false
        }
    }
}
