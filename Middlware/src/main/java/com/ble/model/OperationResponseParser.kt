package com.ble.model

fun parseOperationResponse(bytes: ByteArray): OperationResponseModel {
    if (bytes.size < 4) throw IllegalArgumentException("Response too short")
    val responseStatusCode = ((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)
    val operationID = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
    return if (responseStatusCode == 0x0000) {
        OperationResponseModel(
            responseStatusCode = responseStatusCode,
            operationID = operationID,
            specialErrorCode = null,
            value = bytes.sliceArray(4 until bytes.size)
        )
    } else {
        OperationResponseModel(
            responseStatusCode = responseStatusCode,
            operationID = operationID,
            specialErrorCode = bytes.sliceArray(4 until bytes.size),
            value = null
        )
    }
}

