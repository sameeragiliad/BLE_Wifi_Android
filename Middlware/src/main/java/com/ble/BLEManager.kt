package com.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
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
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.ble.api.BLEApi
import com.ble.model.BleScanResult
import com.ble.model.ConnectionState
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
    private var lastService: BluetoothGattService? = null
    private val errorCallbacks = mutableSetOf<(String) -> Unit>()
    private var connectedDeviceAddress: String? = null

    private val profileUuid = ParcelUuid.fromString("6DF733E0-AC7B-4C63-8226-FFE665B82697")

    // Service and characteristic UUIDs
    private val primaryServiceUuid = java.util.UUID.fromString("6DF733E0-AC7B-4C63-8226-FFE665B82697")
    private val machineEnableWiFiCharacteristicUUID = java.util.UUID.fromString("6DF733E1-AC7B-4C63-8226-FFE665B82697")
    private val machineWiFiSSIDCharacteristicUUID = java.util.UUID.fromString("6DF733E2-AC7B-4C63-8226-FFE665B82697")
    private val machineWiFiPasswordCharacteristicUUID = java.util.UUID.fromString("6DF733E3-AC7B-4C63-8226-FFE665B82697")

    private val connectionCallbacks = mutableSetOf<(ConnectionState) -> Unit>()

    private var currentlyConnectedDevice: String? = null

    override fun startScan() {
        if (!hasBluetoothPermissions()) return
        try {
            android.util.Log.e("BLEManager", "Scan initiated:")
            val filter = ScanFilter.Builder()
                .setServiceUuid(profileUuid)
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)

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
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("BLEManager", "Scan result: ${result.device.address}, RSSI: ${result.rssi}, scanRecord: ${result}")
            val scanRecord = result.scanRecord ?: return
            // Filter by Profile ID in service UUIDs
            if (scanRecord.serviceUuids?.contains(profileUuid) == true) {
                val mac = result.device.address
                val rssi=result.rssi
                var assetName = result.device.name?:""
                val bleResult = BleScanResult(mac, assetName, rssi)
                scanResultsFlow.tryEmit(bleResult)
                scanCallbacks.forEach { it(bleResult) }
            }
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
        currentlyConnectedDevice = deviceAddress
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnectBle(gatt: BluetoothGatt) {
        stopScan()
        gatt.disconnect()
        gatt.close()
        connectedDeviceAddress = null
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disableWiFi() {
        val gatt = bluetoothGatt
        val service = lastService
        if (gatt != null && service != null) {
            disableWiFi(gatt, service)
        } else {
            notifyError("Cannot disable WiFi: GATT or Service not available")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
       initiateDisconnection()
    }

    private fun setConnectionState(state: ConnectionState) {
        connectionState = state
        notifyConnectionState(state)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public fun enableWiFi(gatt: BluetoothGatt, service: BluetoothGattService) {

        val requestChar = service.getCharacteristic(machineEnableWiFiCharacteristicUUID)
        if (requestChar == null) {
            notifyError("Request characteristic not found")
            gatt.disconnect()
            return
        }
        val opcode = byteArrayOf(0x01) // 0x0001
       // val operatorIdBytes = operatorId.chunked(2).map { it.toInt(16).toByte() }.toByteArray() // 6 bytes
       // val request = ByteArray(8)
      //  System.arraycopy(opcode, 0, request, 0, 2)
       // System.arraycopy(operatorIdBytes, 0, request, 2, 6)
        requestChar.value = opcode
        requestChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(requestChar)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun disableWiFi(gatt: BluetoothGatt, service: BluetoothGattService) {
        val requestChar = service.getCharacteristic(machineEnableWiFiCharacteristicUUID)
        if (requestChar == null) {
            notifyError("Request characteristic not found")
            disconnectGatt(gatt)
            return
        }
        val opcode = byteArrayOf(0x00) // 0x0001
        requestChar.value = opcode
        requestChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(requestChar)
        // BLE disconnect after disabling WiFi
        disconnectGatt(gatt)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun disconnectGatt(gatt: BluetoothGatt) {
        gatt.disconnect()
        gatt.close()
        bluetoothGatt = null
        connectedDeviceAddress = null
        connectionState = ConnectionState.DISCONNECTED
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun enableSSIDCharacteristicNotification(gatt: BluetoothGatt, service: BluetoothGattService) {
        android.util.Log.d("BLEManager", "enableResponseCharacteristicNotification")
        //val device = gatt.device
        val responseChar = service.getCharacteristic(machineWiFiSSIDCharacteristicUUID)
        val props = responseChar.properties


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
                    descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    val writeOk = gatt.writeDescriptor(descriptor)
                    android.util.Log.d("BLEManager", "writeDescriptor called for ${descriptor.uuid} and  ${descriptor.value}, result: $writeOk")
                }
            }, 300)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun enablePasswordCharacteristicNotification(gatt: BluetoothGatt, service: BluetoothGattService) {
        android.util.Log.d("BLEManager", "enableResponseCharacteristicNotification")
        //val device = gatt.device
        val responseChar = service.getCharacteristic(machineWiFiPasswordCharacteristicUUID)
        val props = responseChar.properties


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
                    descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
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
               // setConnectionState(ConnectionState.CONNECTED)
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
            lastService = service
            setConnectionState(ConnectionState.SERVICE_DISCOVERED)

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                enablePasswordCharacteristicNotification(gatt, service)
            }, 0)

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                enableSSIDCharacteristicNotification(gatt, service)
            }, 1000)

           /* val protocolChar = service.getCharacteristic(protocolVersionCharUuid)
            if (protocolChar == null) {
                setConnectionState(ConnectionState.ERROR)
                notifyError("Protocol version characteristic not found")
                return
            }
            safeReadCharacteristic(gatt, protocolChar)*/

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                enableWiFi(gatt, service)
            }, 2000)

        }



        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Log.d("BLEManager", "onDescriptorWrite ${descriptor.uuid}, status: $status")
            if (descriptor.characteristic.uuid == machineWiFiSSIDCharacteristicUUID) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLEManager", "Response characteristic notification enabled successfully.")

                } else {
                    Log.e("BLEManager", "Failed to enable response characteristic notification. Status: $status")
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        @Deprecated("Deprecated in Java")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            android.util.Log.d("BLEManager", "onCharacteristicChanged "+characteristic.uuid)
            when (characteristic.uuid) {
                machineWiFiSSIDCharacteristicUUID -> {
                    val data = characteristic.value ?: return
                    val ssid = data.toString(Charsets.UTF_8)
                    android.util.Log.d("BLEManager", "Discovered WiFi SSID Characteristic: $ssid")
                    if (wifiCredentials == null) wifiCredentials = WiFiCredentials()
                    wifiCredentials?.ssid = ssid
                    tryConnectToWifi()
                }
                machineWiFiPasswordCharacteristicUUID -> {
                    val data = characteristic.value ?: return
                    val password = data.toString(Charsets.UTF_8)
                    android.util.Log.d("BLEManager", "Discovered WiFi Password Characteristic: $password")
                    if (wifiCredentials == null) wifiCredentials = WiFiCredentials()
                    wifiCredentials?.password = password
                    tryConnectToWifi()
                }
                else -> {
                    android.util.Log.d("BLEManager", "characteristic: ${characteristic.uuid}")
                }
            }
        }

    }



    data class WiFiCredentials(var ssid: String = "", var password: String = "")
    private var wifiCredentials: WiFiCredentials? = null
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun tryConnectToWifi() {
        val creds = wifiCredentials
        if (creds != null && creds.ssid.isNotEmpty() && creds.password.isNotEmpty()) {

            // static password, To Do: comment this
            val ssidTemp="Chandler_Bing_5G"
            val passTemp="Saaz0710"
            com.ble.wifi.connectToWifi(context, ssidTemp, passTemp) {
                notifyConnectionState(ConnectionState.CONNECTED)
            }
            //.........................................

            // To Do: uncomment this
            /*com.ble.wifi.connectToWifi(context, creds.ssid, creds.password) {
                notifyConnectionState(ConnectionState.CONNECTED)
            }*/

            android.util.Log.d("BLEManager", "Attempting WiFi connection with SSID: ${creds.ssid}")

            // Notify connection state as CONNECTED

        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun initiateDisconnection() {
        val device = bluetoothAdapter?.getRemoteDevice(currentlyConnectedDevice)
        val bluetoothGatt = device!!.connectGatt(context, false, gattCallback)
        val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to GATT server.")
                    // You now have the BluetoothGatt object
                    val connectedGatt = gatt
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from GATT server.")
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onServicesDiscovered(
                gatt: BluetoothGatt?,
                status: Int
            ) {
                val service = gatt?.getService(primaryServiceUuid)
                if (service == null) {
                    notifyError("Primary service not found")
                    return
                }
                disconnectWifi(gatt, service)
                //disconnect(currentlyConnectedDevice?:"")
                disconnectBle(gatt)
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                Log.d("BLEManager", "onDescriptorWrite ${descriptor?.uuid}, status: $status")
                if (descriptor?.characteristic?.uuid == machineWiFiSSIDCharacteristicUUID) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("BLEManager", "Wifi disable characteristic written successfully")

                    } else {
                        Log.e("BLEManager", "Failed to write disable wifi characteristic: $status")
                    }
                }
            }
        }

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun disconnectWifi(gatt: BluetoothGatt, service: BluetoothGattService) {

        val requestChar = service.getCharacteristic(machineEnableWiFiCharacteristicUUID)
        if (requestChar == null) {
            notifyError("Request characteristic not found")
            gatt.disconnect()
            return
        }
        val opcode = byteArrayOf(0x00) // 0x0001
        requestChar.value = opcode
        requestChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(requestChar)
    }
}
