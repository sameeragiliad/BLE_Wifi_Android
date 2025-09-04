package com.agiliad.blewifi.nearbydevices.repository.implementation

import com.agiliad.blewifi.nearbydevices.model.Device
import com.agiliad.blewifi.nearbydevices.repository.interfaces.DeviceEmitter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class FakeDeviceEmitter : DeviceEmitter {
    private val allDevices = listOf(
        Device("", "Mini Excavators306 CR", -50, "Excavator", "", false, false),
        Device("","CATD3", -51,"Dozer",""),
        Device("","CATD31", -52,"Dozer2",""),
        Device("","CATD32", -53,"Dozer3",""),
        Device("","CATD33", -54,"Dozer4",""),
        Device("","CATD34", -55,"Dozer5",""),
        Device("","CATD35", -56,"Dozer6",""),
        Device("","CATD36", -57,"Dozer7",""),
    )

    override fun deviceStream(): Flow<List<Device>>  = flow {
        var current = emptyList<Device>()

        while (true) {
            delay(300)
            current = current.toMutableList().apply {
                val add = Random.nextBoolean()
                if (add && size < allDevices.size) {
                    allDevices.firstOrNull { it !in this }?.let { add(it) }
                } else if (isNotEmpty()) {

                }
            }
            emit(current)
        }
    }
}