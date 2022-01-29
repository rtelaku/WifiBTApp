package com.rtelaku.wifibtapp.ui.bluetooth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rtelaku.wifibtapp.adapters.AvailableBluetoothAdapter
import com.rtelaku.wifibtapp.databinding.ActivityBluetoothActivityBinding
import com.rtelaku.wifibtapp.services.OnClick
import com.rtelaku.wifibtapp.utils.LocationInteractor
import com.rtelaku.wifibtapp.utils.ToastUtils
import com.rtelaku.wifibtapp.utils.WifiBTApplication

class BluetoothActivity : AppCompatActivity() , OnClick {

    private lateinit var availableBluetoothAdapter: AvailableBluetoothAdapter
    private lateinit var binding: ActivityBluetoothActivityBinding
    private lateinit var bluetoothViewModel: BluetoothViewModel

    private val MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1
    private val MY_PERMISSIONS_ACCESS_FINE_LOCATION = 100
    private var hasGps = WifiBTApplication.hasGps()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothActivityBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        bluetoothViewModel = ViewModelProvider(this)[BluetoothViewModel::class.java]

        setupRecycleView()
        checkBluetoothState()
        scanBluetooth()
        loadBluetoothList()
    }

    private fun setupRecycleView() {
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.bluetoothRecycleView.addItemDecoration(dividerItemDecoration)
        availableBluetoothAdapter = AvailableBluetoothAdapter(this)
        binding.bluetoothRecycleView.layoutManager = LinearLayoutManager(this)
        binding.bluetoothRecycleView.adapter = availableBluetoothAdapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_COARSE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothViewModel.getBluetooth()
            } else {
                ToastUtils.displayToast(this, "Permission not granted")
                return
            }
            else -> throw IllegalStateException("Unexpected value: $requestCode")
        }
    }

    private fun scanBluetooth() {
        if (Build.VERSION.SDK_INT >= 23 &&
            ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@BluetoothActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(
                this@BluetoothActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_ACCESS_COARSE_LOCATION)
        } else {
            if(hasGps) {
                LocationInteractor.checkLocationStateAndScan(bluetoothViewModel.getBluetooth(), resources, this)
            } else {
                bluetoothViewModel.getBluetooth()
            }
        }
    }

    private fun loadBluetoothList() {
        bluetoothViewModel.bluetoothDevice.observe(this, { data  ->
            availableBluetoothAdapter.addBluetoothList(data)
            binding.spinner.visibility = View.GONE
        })
    }

    override fun onResume() {
        super.onResume()
        checkBluetoothState()
        if(hasGps) {
            LocationInteractor.checkLocationStateAndScan(bluetoothViewModel.getBluetooth(), resources, this)
        }
    }

    private fun checkBluetoothState() {
       checkBluetoothStateOnCreate()

        bluetoothViewModel.bluetoothState.observe(this, { isEnabled ->
            if(!isEnabled) {
                availableBluetoothAdapter.clearList()
                binding.bluetoothRecycleView.visibility = View.GONE
                showSnackBar()
            } else {
                binding.bluetoothRecycleView.visibility = View.VISIBLE
            }
        })
    }

    private fun checkBluetoothStateOnCreate() {
        if(!bluetoothViewModel.isBluetoothEnabled()) {
            binding.spinner.visibility = View.GONE
            availableBluetoothAdapter.clearList()
            binding.bluetoothRecycleView.visibility = View.GONE
            showSnackBar()
        } else {
            binding.spinner.visibility = View.VISIBLE
            binding.bluetoothRecycleView.visibility = View.VISIBLE
        }
    }

    private fun showSnackBar() {
            val bluetoothSnackBar = Snackbar
                .make(binding.bluetoothRoot, "Please turn on your bluetooth", Snackbar.LENGTH_LONG)
                .setAction("OK") {
                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                }

            bluetoothSnackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothViewModel.closeDiscovery()
        bluetoothViewModel.unregisterBluetooth()
    }
}

