package com.agiliad.blewifi.nearbydevices.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.ble.api.BLEApi
import com.ble.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.agiliad.blewifi.nearbydevices.model.Device
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _scanning = MutableStateFlow(false)
    private var autoConnectJob: Job? = null
    private val _connecting = MutableStateFlow<String?>(null)
    val connecting: StateFlow<String?> = _connecting.asStateFlow()
    var count:Int = 0

    private val seenDevices = mutableSetOf<String>()

    private val autoConnectTimer = 500000000L

    var currentConnectedDevice: Device? = null
    fun initScan() {
        // Store operator ID as "1" in shared preferences on launch
        prefs.edit { putString("operator_id", "000000000001") }

        bleApi.registerScanCallback { result ->
           val device = Device(
                name = result.assetName,
                signalStrength = result.rssi, // You can add RSSI if available
                type = "BLE",
                mac = result.macAddress,
                id = result.macAddress,
                isFavorite = isFavorite(result.macAddress),
                isPreviouslyConnected = isPreviouslyConnected(result.macAddress)
            )
            if(seenDevices.add(device.mac)) {
                _devices.value = _devices.value + device
            }

           // _devices.value = listOf(device)
             //   .plus(_devices.value.filter { it.mac != result.macAddress })
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

    fun startScanAndAutoConnectWindow() {

        count++
        println("counter for scan $count")
       if(!_scanning.value) {
           initScan()
           autoConnectJob?.cancel()
           autoConnectJob = viewModelScope.launch {
               while (devices.value.isEmpty()) {
                   delay(100)
               }

               delay(autoConnectTimer)
               println("outside connection : $_connecting.value} ")
               if (_connecting.value == null) {
                   val top = getHighestRssiDevice()
                   println("Inside connection : ${top?.mac} ")
                   top?.let {
                       connectToDevice(it)
                   }
               }

           }
       }
    }

    fun getHighestRssiDevice(): Device? {
        return _devices.value.maxByOrNull{ it.signalStrength }
    }
    fun connectToDevice(device: Device) {
        currentConnectedDevice = device
        autoConnectJob?.cancel()
        _connecting.value = device.mac
        bleApi.connect(device.mac)
        markAsPreviouslyConnected(device.mac)

        // Update the device's connectionState in the _devices list
        _devices.value = _devices.value.map {
            if (it.mac == device.mac) it.copy(connectionState = "Connecting") else it
        }
        //connectionState = ConnectionState.CONNECTING;
    }

    fun onDeviceTapped(device: Device, onNavigateToDashboard: () -> Unit) {
        // If the tapped device is already connected, go to dashboard
        if (device.connectionState == "CONNECTED") {
            onNavigateToDashboard()
            return
        }
        // If another device is connected, disconnect first
        val connectedDevice = _devices.value.find { it.connectionState == "CONNECTED" }
        if (connectedDevice != null && connectedDevice.mac != device.mac) {
            bleApi.disconnect()
            // Optionally, wait for disconnect to complete before connecting
        }
        connectToDevice(device)
    }

    override fun onCleared() {
        _scanning.value = false
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
        bleApi.stopScan()
        // Trigger any additional logic for when the device is connected
    }
}