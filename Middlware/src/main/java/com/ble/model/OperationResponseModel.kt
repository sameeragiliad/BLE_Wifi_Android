package com.ble.model

/**
 * Model to represent parsed BLE operation response
 */
data class OperationResponseModel(
    val responseStatusCode: Int,
    val operationID: Int,
    val specialErrorCode: ByteArray?,
    val value: ByteArray?
)

