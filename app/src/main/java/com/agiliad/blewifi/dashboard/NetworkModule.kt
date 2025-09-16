package com.agiliad.blewifi.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import dagger.Module
import dagger.Provides

import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
      //  val gatewayIp = getAccessPointIp(context)
        return Retrofit.Builder()
           // .baseUrl("http://10.0.2.2:8000/") //192.168.137.96
            .baseUrl("http://192.168.4.1:8000/")
           // .baseUrl("http://raspberrypi.local:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @SuppressLint("ServiceCast", "DefaultLocale")
    fun getAccessPointIp(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifiManager.dhcpInfo ?: return null

        val gatewayIp = dhcpInfo.gateway
        return String.format(
            "%d.%d.%d.%d",
            gatewayIp and 0xff,
            gatewayIp shr 8 and 0xff,
            gatewayIp shr 16 and 0xff,
            gatewayIp shr 24 and 0xff
        )
    }

}
