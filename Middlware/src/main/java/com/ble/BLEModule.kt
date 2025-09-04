package com.ble

import android.content.Context
import com.ble.api.BLEApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BLEModule {
    @Provides
    @Singleton
    fun provideBLEApi(@ApplicationContext context: Context): BLEApi {
        return BLEManager(context)
    }
}
