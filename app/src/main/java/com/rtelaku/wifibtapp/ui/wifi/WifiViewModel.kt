package com.rtelaku.wifibtapp.ui.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rtelaku.wifibtapp.utils.ToastUtils
import com.rtelaku.wifibtapp.utils.WifiBTApplication
import com.rtelaku.wifibtapp.utils.WifiInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class WifiViewModel : ViewModel() {
    private var wifiManager: WifiManager = WifiBTApplication.getInstance().applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    private val compositeDisposable = CompositeDisposable()
    private var selectedWifi: ScanResult? = null
    private val TAG = "WifiViewModel"

    val wifiScanResultLD = MutableLiveData<ArrayList<ScanResult>>()
    var wifiState = MutableLiveData<Boolean>()
    var connectedWifi = MutableLiveData<String>()

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val listOfWifi: ArrayList<ScanResult> = ArrayList()

            when (action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    onChangeWifiState(intent)
                }

                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    onWifiScanResult(listOfWifi)
                }

                WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> {
                    checkErrorAuthenticating(intent)
                }
            }
        }
    }

    private fun onChangeWifiState(intent: Intent) {
        val wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
            WifiManager.WIFI_STATE_UNKNOWN)
        when (wifiStateExtra) {
            WifiManager.WIFI_STATE_ENABLED -> {
                wifiState.postValue(true)
                getWifi()
            }
            WifiManager.WIFI_STATE_DISABLED -> {
                wifiState.postValue(false)
            }
        }
    }

    private fun onWifiScanResult(listOfWifi: ArrayList<ScanResult>) {
        val wifiList = wifiManager.scanResults

        for (scanResult in wifiList) {
            if (!scanResult.SSID.isNullOrEmpty()) {
                listOfWifi.add(scanResult)
            }
        }

        val filteredList = listOfWifi.distinctBy { it.SSID } as ArrayList<ScanResult>
        wifiScanResultLD.postValue(filteredList)
    }

    private fun checkErrorAuthenticating(intent: Intent) {
        val supplicantError = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1)
        if (supplicantError == WifiManager.ERROR_AUTHENTICATING) {
            ToastUtils.displayToast(WifiBTApplication.getInstance(), "Incorrect password")
            WifiInteractor.checkWifiPassword(true, getCurrentWifiConfiguration(selectedWifi)[0])
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentWifiConfiguration(networkModel: ScanResult?): List<WifiConfiguration> {
        selectedWifi = networkModel
        return wifiManager.configuredNetworks.filter { wifi -> wifi.SSID == "\"" + networkModel?.SSID + "\"" }
    }

    private fun getConnectedWifi(): String {
        val ssid = wifiManager.connectionInfo.ssid
        return ssid.replace("\"", "")
    }

    fun onConnectivityAction() {
        connectedWifi.postValue(getConnectedWifi())
    }

    init {
        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        }
        WifiBTApplication.getInstance().applicationContext.registerReceiver(wifiReceiver, intentFilter)
        wifiState.postValue(wifiManager.isWifiEnabled)
        connectedWifi.postValue(wifiManager.connectionInfo.ssid)
        if (wifiManager.isWifiEnabled) {
            getWifi()
        }
    }

    fun unregisterReceiver() {
        WifiBTApplication.getInstance().applicationContext.unregisterReceiver(wifiReceiver)
    }

    fun getWifi() {
        compositeDisposable.add(
            Observable.interval(1, 300, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe({
                    if (wifiManager.isWifiEnabled) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val filteredList = wifiManager.scanResults.distinctBy { it.SSID } as ArrayList<ScanResult>

                            wifiScanResultLD.postValue(filteredList)
                            wifiManager.startScan()

                        } else {
                            wifiManager.startScan()
                        }
                    } else {
                        Log.d(TAG, it.toString())
                    }
                }, { throwable ->
                    Log.d(TAG, "No data! ${throwable.localizedMessage}", throwable)
                })
        )
    }

    fun turnOnWifi() {
        wifiManager.isWifiEnabled = true
    }

    fun turnOffWifi() {
        wifiManager.isWifiEnabled = false
    }

    fun closeDisposable() {
        compositeDisposable.clear()
    }
}