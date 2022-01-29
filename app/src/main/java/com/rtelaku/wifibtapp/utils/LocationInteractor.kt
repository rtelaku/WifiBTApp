package com.rtelaku.wifibtapp.utils

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.location.LocationManager
import android.provider.Settings

object LocationInteractor {

    private fun showSettingsAlert(resources: Resources, context: Context) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setTitle("Please enable your location to use the app!")
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(
            resources.getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            })
        alertDialog.show()
    }

    fun checkLocationStateAndScan(function: Unit, resources: Resources, context: Context){
        val locationManager = WifiBTApplication.getInstance().applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            function
        } else {
            showSettingsAlert(resources, context)
        }
    }
}