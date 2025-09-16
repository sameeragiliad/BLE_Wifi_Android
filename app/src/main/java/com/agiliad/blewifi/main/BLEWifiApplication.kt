package com.agiliad.blewifi.main

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.ble.api.BLEApi
import com.ble.wifi.disconnectFromWifi
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BLEWifiApplication: Application() {

    @Inject
    lateinit var ble: BLEApi

    override fun onCreate() {
        super.onCreate()

        val observer = AppLifecycleObserver {
            // Disconnect BLE and Wi-Fi here
          //  disconnectBle()
           // disconnectWifi()
         disconnectWifi()
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    private fun disconnectBle() {
        // Your BLE disconnection logic
    }

    private fun disconnectWifi() {
        // Your Wi-Fi disconnection logic
        println("Wifi in disconnectWifi")
       // disconnectFromWifi(applicationContext)
        ble.disconnect()
    }

}