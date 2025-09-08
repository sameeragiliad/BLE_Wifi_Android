package com.agiliad.blewifi.utils.permissions

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberMultiplePermissionState(
    permissions: Array<String>,
    onPermissionsGranted: () -> Unit = {},
    onPermissionsDenied: (List<String>) -> Unit = {}
): PermissionState {
    val context = LocalContext.current
    val state = remember {
        PermissionState(permissions, onPermissionsGranted, onPermissionsDenied)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        state.allGranted = allGranted

        if (allGranted) {
            onPermissionsGranted()
        } else {
            val denied = results.filter { !it.value }.keys.toList()
            state.deniedPermissions = denied
            state.shouldShowRationale = denied.any { permission ->
                try {
                    (context as Activity).shouldShowRequestPermissionRationale(permission)
                } catch (e: Exception) {
                    false
                }
            }
            onPermissionsDenied(denied)
        }
    }

    // Check initial permission status
    LaunchedEffect(permissions) {
        state.allGranted = PermissionUtils.checkPermissions(context, permissions)
    }

    // Initialize the request function
    state.requestPermissions = {
        launcher.launch(permissions)
    }

    return state
}