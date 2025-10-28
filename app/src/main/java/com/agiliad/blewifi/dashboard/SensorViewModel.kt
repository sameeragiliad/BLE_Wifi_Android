package com.agiliad.blewifi.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SensorViewModel @Inject constructor(
    private val repository: SensorRepository,
    private val bleApi: com.ble.api.BLEApi // Inject BLEApi
): ViewModel() {

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData

    private var backgroundJob: Job? = null
    private val _dashboardBackgroundEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dashboardBackgroundEvent = _dashboardBackgroundEvent.asSharedFlow()

    /*
    fun loadSensorData() {
        viewModelScope.launch {
            try {
                val data = repository.fetchAllSensorData()
                _sensorData.value = data
            } catch (e: Exception) {
                // Handle error
            }
        }
    }*/

    private val _downloadStatus = MutableStateFlow<Boolean?>(null)
    val downloadStatus: StateFlow<Boolean?> = _downloadStatus

    init {
        startPolling()
    }
    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val data = repository.fetchAllSensorData()
                    _sensorData.value = data
                } catch (e: Exception) {
                    print("Exception in api call ${e.message}")
                }
                delay(2000)
            }
        }
    }

    fun disconnect() {
        bleApi.disableWiFi()
    }

    fun onDashboardBackgrounded() {
        backgroundJob?.cancel()
        backgroundJob = viewModelScope.launch {
            delay(15000)
            bleApi.disableWiFi()
            _dashboardBackgroundEvent.tryEmit(Unit)
        }
    }

    fun onDashboardForegrounded() {
        backgroundJob?.cancel()
    }
    fun downloadLogToDownloads(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.downloadDiagnosticsToDownloads(context)
            _downloadStatus.value = success
        }
    }
}
