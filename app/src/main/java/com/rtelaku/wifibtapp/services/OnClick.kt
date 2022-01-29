package com.rtelaku.wifibtapp.services

import android.bluetooth.BluetoothDevice
import android.net.wifi.ScanResult

interface OnClick {
    fun wifiOnClickListener() {}

    fun wifiDetailsClickListener(wifi: ScanResult) {}

    fun wifiSelectedClickListener(wifi: ScanResult) {}

    fun bluetoothOnClickListener() {}

    fun bluetoothDetailsClickListener(bluetooth: BluetoothDevice) {}
}