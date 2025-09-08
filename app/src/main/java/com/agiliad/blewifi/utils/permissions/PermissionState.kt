package com.agiliad.blewifi.utils.permissions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PermissionState(
    val permissions: Array<String>,
    val onPermissionsGranted: () -> Unit = {},
    val onPermissionsDenied: (List<String>) -> Unit = {}
) {
    var allGranted by mutableStateOf(false)
    var deniedPermissions by mutableStateOf<List<String>>(emptyList())
    var shouldShowRationale by mutableStateOf(false)
    lateinit var requestPermissions: () -> Unit
}