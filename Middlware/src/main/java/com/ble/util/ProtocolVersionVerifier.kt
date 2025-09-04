package com.ble.util

import android.content.Context
import android.content.SharedPreferences

object ProtocolVersionVerifier {
    fun verify(context: Context, value: ByteArray): Boolean {
        if (value.size < 2) return false
        val protocolVersion = ((value[0].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
        val prefs: SharedPreferences = context.getSharedPreferences("protocol_prefs", Context.MODE_PRIVATE)
        val expectedVersion = 0//prefs.getInt("protocol_version", -1)
        return protocolVersion == expectedVersion
    }
    fun getProtocolVersion(value: ByteArray): Int? {
        return if (value.size >= 2) {
            ((value[0].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
        } else null
    }
}

