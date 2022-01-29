package com.rtelaku.wifibtapp.utils

import android.app.Application
import android.content.pm.PackageManager

class WifiBTApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        wifiAppInstance = this
    }

    companion object {
        lateinit var wifiAppInstance: WifiBTApplication
        fun getInstance(): WifiBTApplication {
            return wifiAppInstance
        }

        fun hasGps() : Boolean {
            return wifiAppInstance.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        }
    }
}