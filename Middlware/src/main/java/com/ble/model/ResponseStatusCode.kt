package com.ble.model

enum class ResponseStatusCode(val code: Int) {
    SUCCESS(0x0000),
    ERROR(0xFFFF),
    UNKNOWN(-1);

    companion object {
        fun fromCode(code: Int): ResponseStatusCode {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

