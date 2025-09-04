package com.agiliad.blewifi.nearbydevices.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.ble.api.BLEApi
import com.ble.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.core.content.edit
import com.agiliad.blewifi.nearbydevices.model.Device

@HiltViewModel
public class NearbyDevicesViewModel @Inject constructor(
    private val bleApi: BLEApi,
    application: Application
) : ViewModel() {
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val prefs: SharedPreferences = application.getSharedPreferences("protocol_prefs", Context.MODE_PRIVATE)
    private val FAVORITE_KEY = "favorite_macs"
    private var favoriteMacs: MutableSet<String> = prefs.getStringSet(FAVORITE_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
    private val PREVIOUSLY_CONNECTED_KEY = "previously_connected_mac"
    private var previouslyConnectedMac: String? = prefs.getString(PREVIOUSLY_CONNECTED_KEY, null)

    init {
        // Store operator ID as "1" in shared preferences on launch
        prefs.edit { putString("operator_id", "000000000001") }

        bleApi.registerScanCallback { result ->
            val device = Device(
                name = result.assetName,
                signalStrength = 0, // You can add RSSI if available
                type = "BLE",
                mac = result.macAddress,
                id = result.macAddress,
                isFavorite = isFavorite(result.macAddress),
                isPreviouslyConnected = isPreviouslyConnected(result.macAddress)
            )
            _devices.value = listOf(device)
                .plus(_devices.value.filter { it.mac != result.macAddress })
        }

        bleApi.registerConnectionCallback { state ->
            _connectionState.value = state
            if (state == ConnectionState.CONNECTED) {
                onDeviceConnected()
            } else if (state == ConnectionState.DISCONNECTED) {

            }
        }
        bleApi.startScan()
    }

    fun connectToDevice(device: Device) {
        bleApi.connect(device.mac)
        markAsPreviouslyConnected(device.mac)
        device.connectionState = "Connecting"
        //connectionState = ConnectionState.CONNECTING;
    }

    override fun onCleared() {
        bleApi.stopScan()
        bleApi.deregisterConnectionCallback { state ->
            _connectionState.value = state
        }
        super.onCleared()
    }

    fun markAsFavorite(mac: String) {
        favoriteMacs.add(mac)
        prefs.edit().putStringSet(FAVORITE_KEY, favoriteMacs).apply()
        updateDeviceFavorite(mac, true)
    }

    fun unmarkAsFavorite(mac: String) {
        favoriteMacs.remove(mac)
        prefs.edit().putStringSet(FAVORITE_KEY, favoriteMacs).apply()
        updateDeviceFavorite(mac, false)
    }

    fun isFavorite(mac: String): Boolean = favoriteMacs.contains(mac)

    private fun updateDeviceFavorite(mac: String, isFavorite: Boolean) {
        _devices.value = _devices.value.map {
            if (it.mac == mac) it.copy(isFavorite = isFavorite) else it
        }
    }

    fun markAsPreviouslyConnected(mac: String) {
        previouslyConnectedMac = mac
        prefs.edit().putString(PREVIOUSLY_CONNECTED_KEY, mac).apply()
        updateDevicePreviouslyConnected(mac, true)
    }

    fun isPreviouslyConnected(mac: String): Boolean = previouslyConnectedMac == mac

    private fun updateDevicePreviouslyConnected(mac: String, isConnected: Boolean) {
        _devices.value = _devices.value.map {
            if (it.mac == mac) it.copy(isPreviouslyConnected = isConnected) else it
        }
    }

    private fun onDeviceConnected() {
        // Trigger any additional logic for when the device is connected
    }
}