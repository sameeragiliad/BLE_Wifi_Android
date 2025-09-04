package com.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.ble.api.BLEApi
import com.ble.model.BleScanResult
import com.ble.model.ConnectionState
import com.ble.model.OperatorError
import com.ble.model.ResponseStatusCode
import com.ble.model.OperationId
import com.ble.model.parseOperationResponse
import com.ble.util.ProtocolVersionVerifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEManager @Inject constructor(@ApplicationContext private val context: Context) : BLEApi {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val scanResultsFlow = MutableSharedFlow<BleScanResult>()
    private val scanCallbacks = mutableSetOf<(BleScanResult) -> Unit>()

    fun getScanResults(): SharedFlow<BleScanResult> = scanResultsFlow

    private var connectionState = ConnectionState.DISCONNECTED
    private var bluetoothGatt: android.bluetooth.BluetoothGatt? = null
    private val errorCallbacks = mutableSetOf<(String) -> Unit>()
    private var connectedDeviceAddress: String? = null

    private val profileUuid = ParcelUuid.fromString("6DF733E0-AC7B-4C63-8226-FFE665B82697")

    // Service and characteristic UUIDs
    private val primaryServiceUuid = java.util.UUID.fromString("6DF733E0-AC7B-4C63-8226-FFE665B82697")
    private val requestCharUuid = java.util.UUID.fromString("6DF733E1-AC7B-4C63-8226-FFE665B82697")
    private val responseCharUuid = java.util.UUID.fromString("6DF733E2-AC7B-4C63-8226-FFE665B82697")
    private val protocolVersionCharUuid = java.util.UUID.fromString("6DF733E3-AC7B-4C63-8226-FFE665B82697")

    private val connectionCallbacks = mutableSetOf<(ConnectionState) -> Unit>()

    override fun startScan() {
        if (!hasBluetoothPermissions()) return
        try {
            android.util.Log.e("BLEManager", "Scan initiated:")
            val filter = ScanFilter.Builder()
                .setServiceUuid(profileUuid)
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scanner?.startScan(listOf(filter), settings, scanCallback)

            android.util.Log.e("BLEManager", "Scan started:")
        } catch (e: SecurityException) {
            // Handle permission denial gracefully
        }
    }

    override fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Handle permission denial gracefully
        }
    }

    override fun registerScanCallback(callback: (BleScanResult) -> Unit) {
        scanCallbacks.add(callback)
    }

    override fun deregisterScanCallback(callback: (BleScanResult) -> Unit) {
        scanCallbacks.remove(callback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            android.util.Log.d("BLEManager", "Scan result: ${result.device.address}, RSSI: ${result.rssi}, scanRecord: ${result}")
            val scanRecord = result.scanRecord ?: return
            // Filter by Profile ID in service UUIDs
           // if (scanRecord.serviceUuids?.contains(profileUuid) == true) {
                val mac = result.device.address
                val assetName = parseAssetName(scanRecord)
                val bleResult = BleScanResult(mac, assetName)
                scanResultsFlow.tryEmit(bleResult)
                scanCallbacks.forEach { it(bleResult) }
           // }
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        // val hasBluetooth = context.checkSelfPermission(android.Manifest.permission.BLUETOOTH) == android.content.pm.PackageManager.PERMISSION_GRANTED
        // val hasBluetoothAdmin = context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        // For Android 12+
        val hasScan = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
        val hasConnect = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
        return hasLocation && hasScan && hasConnect
    }

    private fun parseAssetName(scanRecord: ScanRecord): String {
        // Scan response data: Complete Local Name (type 0x09, 16 bytes, ASCII Big Endian)
        val deviceName = scanRecord.deviceName;
       // val data = scanRecord.getManufacturerSpecificData(0) ?: return deviceName!!
        //val data = scanRecord.bytes ?: return deviceName!!
        /* if (data.size >= 16) {
             return String(data.sliceArray(0..15), Charsets.US_ASCII)
         }*/
        return ""
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(deviceAddress: String) {
        if (!hasBluetoothPermissions()) {
            notifyError("Missing Bluetooth permissions")
            return
        }
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (device == null) {
            notifyError("Device not found: $deviceAddress")
            return
        }
        connectionState = ConnectionState.CONNECTING
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        connectedDeviceAddress = deviceAddress
        android.util.Log.d("BLEManager", "connect initiated device.$connectedDeviceAddress")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect(deviceAddress: String) {
        if (connectedDeviceAddress == deviceAddress) {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            connectionState = ConnectionState.DISCONNECTED
            connectedDeviceAddress = null
        }
    }

    override fun registerErrorCallback(callback: (String) -> Unit) {
        errorCallbacks.add(callback)
    }

    override fun deregisterErrorCallback(callback: (String) -> Unit) {
        errorCallbacks.remove(callback)
    }

    private fun notifyError(message: String) {
        connectionState = ConnectionState.ERROR
        errorCallbacks.forEach { it(message) }
    }

    private fun notifyConnectionState(state: ConnectionState) {
        connectionCallbacks.forEach { it(state) }
    }

    override fun registerConnectionCallback(callback: (ConnectionState) -> Unit) {
        connectionCallbacks.add(callback)
    }

    override fun deregisterConnectionCallback(callback: (ConnectionState) -> Unit) {
        connectionCallbacks.remove(callback)
    }

    private fun setConnectionState(state: ConnectionState) {
        connectionState = state
        notifyConnectionState(state)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeOperatorId(gatt: BluetoothGatt, service: BluetoothGattService) {
        val prefs: SharedPreferences = context.getSharedPreferences("protocol_prefs", Context.MODE_PRIVATE)
        val operatorId = prefs.getString("operator_id", null)
        android.util.Log.d("BLEManager", "writeOperatorID.$operatorId")
        if (operatorId == null || operatorId.length != 12) { // 6 bytes in hex string
            notifyError("Operator ID not set or invalid")
            gatt.disconnect()
            return
        }
        val requestChar = service.getCharacteristic(requestCharUuid)
        if (requestChar == null) {
            notifyError("Request characteristic not found")
            gatt.disconnect()
            return
        }
        val opcode = byteArrayOf(0x01, 0x00) // 0x0001
        val operatorIdBytes = operatorId.chunked(2).map { it.toInt(16).toByte() }.toByteArray() // 6 bytes
        val request = ByteArray(8)
        System.arraycopy(opcode, 0, request, 0, 2)
        System.arraycopy(operatorIdBytes, 0, request, 2, 6)
        requestChar.value = request
        requestChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(requestChar)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeChallengeRequest(gatt: BluetoothGatt, service: BluetoothGattService, challenge: ByteArray) {
        android.util.Log.d("BLEManager", "initiated write challenge.$challenge")
        val requestChar = service.getCharacteristic(requestCharUuid)
        if (requestChar == null) {
            notifyError("Request characteristic not found for challenge")
            gatt.disconnect()
            return
        }
        val opcode = byteArrayOf(0x00, 0x03) // 0x0003
        //val challenge = ByteArray(16) { if (it == 15) 0x64.toByte() else 0x00 } // 16 bytes, last byte is 100
        val request = ByteArray(18)
        System.arraycopy(opcode, 0, request, 0, 2)
        System.arraycopy(challenge, 0, request, 2, 16)
        requestChar.value = request
        requestChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(requestChar)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun enableResponseCharacteristicNotification(gatt: BluetoothGatt, service: BluetoothGattService) {
        android.util.Log.d("BLEManager", "enableResponseCharacteristicNotification")
        //val device = gatt.device
        val responseChar = service.getCharacteristic(responseCharUuid)
        val props = responseChar.properties

        // without descriptor
       /* if(props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
            if(gatt.setCharacteristicNotification(responseChar, true)) {
                android.util.Log.d("BLEManager", "setCharacteristicNotification called for ${responseChar.uuid}, result: true")
            } else {
                android.util.Log.e("BLEManager", "Failed to set characteristic notification for ${responseChar.uuid}")
            }
        }

        if(props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0){
            if(gatt.setCharacteristicNotification(responseChar, true)) {
                android.util.Log.d("BLEManager", "setCharacteristicNotification called for ${responseChar.uuid}, result: true")
            } else {
                android.util.Log.e("BLEManager", "Failed to set characteristic notification for ${responseChar.uuid}")
            }
        }*/

        if ((props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0 &&
            (props and BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0) {
            android.util.Log.d("BLEManager", "Response characteristic does not support notifications or indications.")
            return
        }

        if (responseChar != null) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if(gatt.setCharacteristicNotification(responseChar, true)) {
                    android.util.Log.d("BLEManager", "setCharacteristicNotification called for ${responseChar.uuid}, result: true")
                } else {
                    android.util.Log.e("BLEManager", "Failed to set characteristic notification for ${responseChar.uuid}")
                }

                val descriptor = responseChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    val writeOk = gatt.writeDescriptor(descriptor)
                    android.util.Log.d("BLEManager", "writeDescriptor called for ${descriptor.uuid} and  ${descriptor.value}, result: $writeOk")
                }
            }, 300)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                setConnectionState(ConnectionState.CONNECTED)
                gatt.discoverServices()
                android.util.Log.d("BLEManager", "connected device.$connectedDeviceAddress")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                setConnectionState(ConnectionState.DISCONNECTED)
                notifyError("Disconnected from device")
                android.util.Log.d("BLEManager", "Disconnected device.$connectedDeviceAddress")
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            android.util.Log.d("BLEManager", "onServicesDiscovered")
            val service = gatt.getService(primaryServiceUuid)
            if (service == null) {
                setConnectionState(ConnectionState.ERROR)
                notifyError("Primary service not found")
                return
            }
            setConnectionState(ConnectionState.SERVICE_DISCOVERED)

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                enableResponseCharacteristicNotification(gatt, service)
            }, 2000)
            val protocolChar = service.getCharacteristic(protocolVersionCharUuid)
            if (protocolChar == null) {
                setConnectionState(ConnectionState.ERROR)
                notifyError("Protocol version characteristic not found")
                return
            }
            safeReadCharacteristic(gatt, protocolChar)
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            android.util.Log.d("BLEManager", "onCharacteristicRead ${characteristic.uuid}")
            if (characteristic.uuid == protocolVersionCharUuid) {
                val value = characteristic.value
                if (value != null) {
                    if (ProtocolVersionVerifier.verify(context, value)) {
                        setConnectionState(ConnectionState.PROTOCOL_VERIFIED)
                        // Write operator ID after protocol verification
                        val service = gatt.getService(primaryServiceUuid)
                        if (service != null) {
                            writeOperatorId(gatt, service)
                        } else {
                            setConnectionState(ConnectionState.ERROR)
                            notifyError("Primary service not found for operator ID write")
                            gatt.disconnect()
                        }
                    } else {
                        val protocolVersion = ProtocolVersionVerifier.getProtocolVersion(value)
                        val prefs: SharedPreferences = context.getSharedPreferences("protocol_prefs", Context.MODE_PRIVATE)
                        val expectedVersion = prefs.getInt("protocol_version", -1)
                        setConnectionState(ConnectionState.ERROR)
                        notifyError("Protocol version mismatch: $protocolVersion != $expectedVersion")
                        gatt.disconnect()
                    }
                } else {
                    setConnectionState(ConnectionState.ERROR)
                    notifyError("Invalid protocol version data")
                    gatt.disconnect()
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Log.d("BLEManager", "onDescriptorWrite ${descriptor.uuid}, status: $status")
            if (descriptor.characteristic.uuid == responseCharUuid) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLEManager", "Response characteristic notification enabled successfully.")
                    // Now safe to read protocol version characteristic
                    val service = gatt.getService(primaryServiceUuid)
                    val protocolChar = service?.getCharacteristic(protocolVersionCharUuid)
                    if (protocolChar != null) {
                        safeReadCharacteristic(gatt, protocolChar)
                    } else {
                        notifyError("Protocol version characteristic not found after notification enabled")
                    }
                } else {
                    Log.e("BLEManager", "Failed to enable response characteristic notification. Status: $status")
                }
            }
        }

        @Deprecated("Deprecated in Java")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            android.util.Log.d("BLEManager", "onCharacteristicChanged ${characteristic.uuid}")
            if (characteristic.uuid == responseCharUuid) {
                val value = characteristic.value ?: return
                val response = parseOperationResponse(value)
                val status = ResponseStatusCode.fromCode(response.responseStatusCode)
                val opcode = OperationId.fromCode(response.operationID)
                android.util.Log.d("BLEManager", "Response received: status=$status, opcode=$opcode, value=${response.value?.contentToString()}")
                when (opcode) {
                    OperationId.GET_OPERATOR_ID -> {
                        when (status) {
                            ResponseStatusCode.SUCCESS -> {
                                if (response.value?.size == 6) {
                                    val service = gatt.getService(primaryServiceUuid)
                                    if (service != null) {
                                        // writeChallengeRequest(gatt, service)
                                    } else {
                                        notifyError("Primary service not found for challenge request")
                                        gatt.disconnect()
                                    }
                                }
                            }
                            ResponseStatusCode.ERROR -> {
                                if (response.specialErrorCode?.size == 2) {
                                    val errorCode = ((response.specialErrorCode[0].toInt() and 0xFF) shl 8) or (response.specialErrorCode[1].toInt() and 0xFF)
                                    val operatorError = OperatorError.fromCode(errorCode)
                                    notifyError(operatorError.message)
                                }
                            }
                            ResponseStatusCode.UNKNOWN -> {}
                        }
                    }
                    OperationId.GET_AUTHENTICATION -> {
                        when (status) {
                            ResponseStatusCode.SUCCESS -> {
                                if (response.value?.size == 16) {
                                    val expected = ByteArray(16) { if (it == 15) 0x64.toByte() else 0x00 }
                                    val service = gatt.getService(primaryServiceUuid)
                                    if (response.value.contentEquals(expected)) {
                                        // Authentication successful, continue connection
                                        writeChallengeRequest(gatt, service, response.value)
                                    } else {
                                        notifyError("Authentication failed: challenge mismatch")
                                        gatt.disconnect()
                                    }
                                }
                            }
                            ResponseStatusCode.ERROR -> {
                                if (response.specialErrorCode?.size == 2) {
                                    val errorCode = ((response.specialErrorCode[0].toInt() and 0xFF) shl 8) or (response.specialErrorCode[1].toInt() and 0xFF)
                                    if (errorCode == 0x0004) {
                                        notifyError("Internal authentication error")
                                        gatt.disconnect()
                                    } else {
                                        notifyError("Unknown authentication error: $errorCode")
                                        gatt.disconnect()
                                    }
                                }
                            }
                            ResponseStatusCode.UNKNOWN -> {}
                        }
                    }
                    else -> {
                        // For all other opcodes, pass response to AssetControlViewModel for UI decisions
                        assetControlCallback?.invoke(response)
                    }
                }
            }
        }

    }

    // Callback for asset control responses
    private var assetControlCallback: ((com.ble.model.OperationResponseModel) -> Unit)? = null

    override fun registerAssetControlCallback(callback: (com.ble.model.OperationResponseModel) -> Unit) {
        assetControlCallback = callback
    }

    override fun deregisterAssetControlCallback() {
        assetControlCallback = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun sendAssetControlOperation(operation: OperationId, value: Boolean) {
        val gatt = bluetoothGatt ?: run {
            notifyError("No active BLE connection for asset control operation")
            return
        }
        val service = gatt.getService(primaryServiceUuid) ?: run {
            notifyError("Primary service not found for asset control operation")
            return
        }
        val opcode = operation.code.toShort()
        val payload: Short = if (value) 0x0001 else 0x0000
        writeCommand(gatt, service, opcode, payload)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeCommand(gatt: BluetoothGatt, service: BluetoothGattService, opcode: Short, payload: Short) {
        android.util.Log.d("BLEManager", "writeCommand called, opcode: $opcode, payload: $payload")
        val requestChar = service.getCharacteristic(requestCharUuid)
        if (requestChar == null) {
            notifyError("Request characteristic not found for command")
            gatt.disconnect()
            return
        }
        val request = ByteArray(4)
        request[0] = (opcode.toInt() and 0xFF).toByte()
        request[1] = ((opcode.toInt() shr 8) and 0xFF).toByte()
        request[2] = (payload.toInt() and 0xFF).toByte()
        request[3] = ((payload.toInt() shr 8) and 0xFF).toByte()
        requestChar.value = request
        requestChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        val success = gatt.writeCharacteristic(requestChar)
        android.util.Log.d("BLEManager", "writeCommand called, result: $success, opcode: $opcode, payload: $payload")
        if (!success) {
            notifyError("Failed to initiate command write")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun safeReadCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic): Boolean {
        val hasReadProperty = (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0
        if (!hasReadProperty) {
            android.util.Log.e("BLEManager", "Characteristic ${characteristic.uuid} does not have PROPERTY_READ")
            notifyError("Characteristic does not support read operation")
            return false
        }
        val result = gatt.readCharacteristic(characteristic)
        android.util.Log.d("BLEManager", "readCharacteristic called for ${characteristic.uuid}, result: $result")
        if (!result) {
            notifyError("Failed to initiate characteristic read for ${characteristic.uuid}")
        }
        return result
    }
}
