package com.ble.wifi

import android.net.wifi.WifiNetworkSpecifier
import android.net.NetworkRequest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.net.Network
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi

var wifiNetworkCallback: ConnectivityManager.NetworkCallback? = null
@RequiresApi(Build.VERSION_CODES.Q)
fun connectToWifi(context: Context, ssid: String, password: String, onConnected: () -> Unit ) {
    val specifier = WifiNetworkSpecifier.Builder()
        .setSsid(ssid)
        .setWpa2Passphrase(password)
        .build()

    val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .setNetworkSpecifier(specifier)
        .build()

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    wifiNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectivityManager.bindProcessToNetwork(network)
            onConnected()
           // wifiNetworkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            print("Wifi onLost")
        }

        override fun onUnavailable() {
            super.onUnavailable()
            print("Wifi onUnavailable")
        }
    }

    wifiNetworkCallback?.let {
        connectivityManager.requestNetwork(request, it) }


    /*connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            connectivityManager.bindProcessToNetwork(network)
            onConnected()
        }
    })*/
}


fun disconnectFromWifi(context: Context) {
    println("WiFi in disconnectFromWifi")
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    wifiNetworkCallback?.let {
        connectivityManager.unregisterNetworkCallback(it)
        wifiNetworkCallback = null
        connectivityManager.bindProcessToNetwork(null)
        println("WiFi in disconnectFromWifi Disconnected from WiFi")
        Toast.makeText(context, "Disconnected from WiFi", Toast.LENGTH_LONG).show()
    }
}

