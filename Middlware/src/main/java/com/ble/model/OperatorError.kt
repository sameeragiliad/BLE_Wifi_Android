package com.ble.model

enum class OperatorError(val code: Int, val message: String) {
    NOT_AUTHORISED(0x0001, "Operator not authorised"),
    NOT_FOUND(0x0002, "Operator not found"),
    NOT_SET(0x0003, "Operator not set"),
    UNKNOWN(-1, "Unknown operator error");

    companion object {
        fun fromCode(code: Int): OperatorError {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

