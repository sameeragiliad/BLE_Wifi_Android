package com.ble.api

import com.ble.model.BleScanResult
import com.ble.model.ConnectionState
import com.ble.model.OperationResponseModel
import com.ble.model.OperationId

interface BLEApi {
    fun startScan()
    fun stopScan()
    fun registerScanCallback(callback: (BleScanResult) -> Unit)
    fun deregisterScanCallback(callback: (BleScanResult) -> Unit)
    fun connect(deviceAddress: String)
    fun disconnect(deviceAddress: String)
    fun registerErrorCallback(callback: (String) -> Unit)
    fun deregisterErrorCallback(callback: (String) -> Unit)
    fun registerConnectionCallback(callback: (ConnectionState) -> Unit)
    fun deregisterConnectionCallback(callback: (ConnectionState) -> Unit)

    fun disconnect()
    //fun registerAssetControlCallback(callback: (OperationResponseModel) -> Unit)
   // fun deregisterAssetControlCallback()
    //fun sendAssetControlOperation(operation: OperationId, value: Boolean)
}
