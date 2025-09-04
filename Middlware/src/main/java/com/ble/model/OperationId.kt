package com.ble.model

enum class OperationId(val code: Int) {
    GET_OPERATOR_ID(0x0002),
    GET_AUTHENTICATION(0x0003),
    SET_DOOR_LOCK(0x0006),
    GET_DOOR_LOCK(0x0007),
    SET_HVAC_STATE(0x000B),
    GET_HVAC_STATE(0x000C),
    SET_WORKLIGHT_STATE(0x0009),
    GET_WORKLIGHT_STATE(0x000A),
    SET_SOUND_HORN(0x0008),
    UNKNOWN(-1);

    companion object {
        fun fromCode(code: Int): OperationId {
            return values().find { it.code == code } ?: UNKNOWN
        }
    }
}

