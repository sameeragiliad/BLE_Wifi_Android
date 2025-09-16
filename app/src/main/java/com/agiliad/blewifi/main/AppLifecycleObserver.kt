package com.agiliad.blewifi.main

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val onBackgroundTooLong: () -> Unit
) : DefaultLifecycleObserver {

    val timerForBackgroudCheck = 15000
    private var backgroundStartTime: Long = 0
    private var isInBackground = false
    private val handler = Handler(Looper.getMainLooper())
    private val backgroundRunnable = Runnable {
        println("Wifi in backgroundRunnable")
        if (isInBackground && System.currentTimeMillis() - backgroundStartTime >= timerForBackgroudCheck) {
            println("Wifi in backgroundRunnable >=15000")
            onBackgroundTooLong()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        println("Wifi onStop timer started")
        backgroundStartTime = System.currentTimeMillis()
        isInBackground = true
        handler.postDelayed(backgroundRunnable, 15000)
    }

    override fun onStart(owner: LifecycleOwner) {
        isInBackground = false
        handler.removeCallbacks(backgroundRunnable)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
    }
}
