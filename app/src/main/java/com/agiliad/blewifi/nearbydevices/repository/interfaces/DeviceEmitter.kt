package com.agiliad.blewifi.nearbydevices.repository.interfaces
import com.agiliad.blewifi.nearbydevices.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceEmitter {
     fun deviceStream(): Flow<List<Device>>
}