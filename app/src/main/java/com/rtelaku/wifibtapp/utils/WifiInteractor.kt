package com.rtelaku.wifibtapp.utils

import android.annotation.SuppressLint
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager

object WifiInteractor {

    private val wifiManager = WifiBTApplication.getInstance().applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

    fun isNetworkConnected(incomingSelectedSSID: String?): Boolean {
        val selectedSSID = String.format("\"%s\"", incomingSelectedSSID)
        val ssid = wifiManager.connectionInfo.ssid
        val connectivityManager = WifiBTApplication.getInstance().applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo

        return activeNetworkInfo != null && activeNetworkInfo.isConnected && selectedSSID == ssid
    }

    fun checkNetworkCapabilities(capability: String): String {
        val capabilities = arrayOf("WEP", "PSK", "EAP")

        for (i in capabilities.indices) {
            if (capability.contains(capabilities[i])) {
                return capabilities[i]
            }
        }
        return "OPEN"
    }

    private fun createWifiConfiguration(ssid: String, password: String?, capability: String): WifiConfiguration? {
        val wifiConfiguration = WifiConfiguration()

        wifiConfiguration.SSID = "\"" + ssid + "\""

        when {
            capability.equals("OPEN", ignoreCase = true) -> {
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
            capability.equals("WEP", ignoreCase = true) -> {
                configureWEP(wifiConfiguration, password)
            }
            capability.equals("PSK", ignoreCase = true) -> {
                configurePSK(wifiConfiguration, password)
            }
            else -> {
                return null
            }
        }
        return wifiConfiguration
    }

    private fun configureWEP(wifiConfiguration: WifiConfiguration, password: String?) {
        wifiConfiguration.wepKeys[0] = "\"" + password + "\""
        wifiConfiguration.wepTxKeyIndex = 0
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
    }

    private fun configurePSK(wifiConfiguration: WifiConfiguration, password: String?) {
        wifiConfiguration.preSharedKey = "\"" + password + "\""
        wifiConfiguration.hiddenSSID = true
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
    }

    fun connectWifi(ssid: String, password: String? = null, securityMode: String) {
        val network = wifiManager.addNetwork(createWifiConfiguration(ssid, password, securityMode));
        wifiManager.enableNetwork(network, true)
        wifiManager.reconnect()
    }

    fun forgetWifi(wifi: WifiConfiguration) {
        wifiManager.removeNetwork(wifi.networkId)
    }

    fun connectExistingWifi(wifiConfiguration: WifiConfiguration) {
        wifiManager.disconnect()
        wifiManager.enableNetwork(wifiConfiguration.networkId, true)
        wifiManager.reconnect()
    }

    @SuppressLint("MissingPermission")
    fun isExistingNetwork(ssid : String?) : Boolean {
        val wifiSSID = "\"" + ssid + "\""
        val list = wifiManager.configuredNetworks
        for (i in list) {
            if(i.SSID.equals(wifiSSID)) {
                return true
            }
        }

        return false
    }

    fun checkWifiPassword(isIncorrect: Boolean, wifiConfiguration: WifiConfiguration){
        if(isIncorrect) {
           forgetWifi(wifiConfiguration)
        }
    }
}