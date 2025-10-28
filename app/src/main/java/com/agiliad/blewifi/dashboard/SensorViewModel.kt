package com.agiliad.blewifi.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SensorViewModel @Inject constructor(
    private val repository: SensorRepository
): ViewModel() {

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData

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
    fun downloadLogToDownloads(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.downloadDiagnosticsToDownloads(context)
            _downloadStatus.value = success
        }
    }
}
