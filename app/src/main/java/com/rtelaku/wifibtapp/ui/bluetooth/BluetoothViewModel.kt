package com.rtelaku.wifibtapp.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rtelaku.wifibtapp.utils.WifiBTApplication
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class BluetoothViewModel : ViewModel() {

    private val bluetoothManager = WifiBTApplication.getInstance().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val compositeDisposable = CompositeDisposable()
    private val TAG = "BluetoothViewModel"

    var bluetoothState = MutableLiveData<Boolean>()
    val bluetoothDevice = MutableLiveData<BluetoothDevice>()

    private val bluetoothReceiver = object : BroadcastReceiver() {
            private val deviceList = ArrayList<BluetoothDevice>()

            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        onBluetoothScan(intent, deviceList)
                    }

                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        onBluetoothState(intent)
                    }

                }
            }
        }

    private fun onBluetoothState(intent: Intent) {
        if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
            bluetoothState.postValue(false)
        } else {
            bluetoothState.postValue(true)
            getBluetooth()
        }
    }

    private fun onBluetoothScan(intent: Intent, deviceList : ArrayList<BluetoothDevice>) {
            getBluetooth()
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            if(!device?.name.isNullOrEmpty() && device != null) {
                bluetoothDevice.postValue(device)
            }
    }

    init {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        WifiBTApplication.getInstance().applicationContext.registerReceiver(bluetoothReceiver, intentFilter)
    }

    fun isBluetoothEnabled(): Boolean {
        val myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return myBluetoothAdapter.isEnabled
    }

    fun getBluetooth() {
        compositeDisposable.add(
            Observable.interval(1, 300, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe ({
                    bluetoothAdapter?.startDiscovery()
                } , { throwable ->
                    Log.d(TAG,"No data! ${throwable.localizedMessage}", throwable)
                } )
        )
    }

    fun closeDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    fun unregisterBluetooth() {
        WifiBTApplication.getInstance().applicationContext.unregisterReceiver(bluetoothReceiver)
    }
}