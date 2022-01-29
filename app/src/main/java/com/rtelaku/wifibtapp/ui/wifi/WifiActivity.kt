package com.rtelaku.wifibtapp.ui.wifi

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rtelaku.wifibtapp.adapters.WifiAdapter
import com.rtelaku.wifibtapp.databinding.ActivityWifiBinding
import com.rtelaku.wifibtapp.models.ConnectionLiveData
import com.rtelaku.wifibtapp.services.OnClick
import com.rtelaku.wifibtapp.ui.connectWifiDialog.ExistingWifiDialog
import com.rtelaku.wifibtapp.ui.connectWifiDialog.WifiDialog
import com.rtelaku.wifibtapp.utils.LocationInteractor
import com.rtelaku.wifibtapp.utils.ToastUtils
import com.rtelaku.wifibtapp.utils.WifiBTApplication
import com.rtelaku.wifibtapp.utils.WifiInteractor
import java.util.*

class WifiActivity : AppCompatActivity(), OnClick {

    private lateinit var wifiRecyclerAdapter: WifiAdapter
    private lateinit var binding: ActivityWifiBinding
    private lateinit var wifiViewModel: WifiViewModel
    private lateinit var selectedWifi: ScanResult
    private lateinit var wifiConfiguration: List<WifiConfiguration>
    private lateinit var dialog: WifiDialog
    private lateinit var existingWifiDialog: ExistingWifiDialog

    private val MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1
    private val MY_PERMISSIONS_ACCESS_FINE_LOCATION = 100
    private val TAG = "WifiActivity"
    private var hasGps = WifiBTApplication.hasGps()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiViewModel = ViewModelProvider(this)[WifiViewModel::class.java]

        setupRecycleView()
        scanWifi()
        loadWifiList()
        highlightConnectedWifi()
        processWifiSwitch()
    }

    private fun setupRecycleView() {
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.wifiRecycleView.addItemDecoration(dividerItemDecoration)
        wifiRecyclerAdapter = WifiAdapter(this)
        binding.wifiRecycleView.layoutManager = LinearLayoutManager(this)
        binding.wifiRecycleView.adapter = wifiRecyclerAdapter
    }

    private fun scanWifi() {
        if (Build.VERSION.SDK_INT >= 23 &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@WifiActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(
                this@WifiActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_ACCESS_COARSE_LOCATION)
        } else {
            if (hasGps) {
                LocationInteractor.checkLocationStateAndScan(wifiViewModel.getWifi(), resources, this)
            } else {
                wifiViewModel.getWifi()
            }
            getConnectionActivity()
        }
    }

    private fun getConnectionActivity() {
        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, { isConnected ->
            if (isConnected != null) {
                wifiViewModel.onConnectivityAction()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_FINE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                wifiViewModel.getWifi()
            } else {
                ToastUtils.displayToast(this, "Permission not granted")
                return
            }
            else -> throw IllegalStateException("Unexpected value: $requestCode")
        }
    }

    private fun loadWifiList() {
        wifiViewModel.wifiScanResultLD.observe(this, { data ->
            wifiRecyclerAdapter.addWifiList(data)
            binding.spinner.visibility = View.GONE
        })
    }

    private fun highlightConnectedWifi() {
        wifiViewModel.connectedWifi.observe(this, { ssid ->
            wifiRecyclerAdapter.updateConnectedWifi(ssid.replace("\"", ""))
        })
    }

    private fun processWifiSwitch() {
        binding.wifiSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            toggleWifiSwitch(isChecked)
        }

        wifiViewModel.wifiState.observe(this, { isEnabled ->
            binding.wifiSwitch.isChecked = isEnabled
            if (!isEnabled) {
                binding.wifiSwitch.text = "OFF"
                wifiRecyclerAdapter.clearList()
                binding.wifiRecycleView.visibility = View.GONE
                binding.spinner.visibility = View.GONE
                dismissDialog()
            } else {
                binding.wifiSwitch.text = "ON"
                binding.spinner.visibility = View.VISIBLE
                binding.wifiRecycleView.visibility = View.VISIBLE
            }
        })
    }

    private fun toggleWifiSwitch(isChecked: Boolean) {
        if (isChecked) {
            wifiViewModel.turnOnWifi()
            binding.wifiSwitch.text = "ON"
        } else {
            wifiViewModel.turnOffWifi()
            binding.wifiSwitch.text = "OFF"
            dismissDialog()
        }
    }

    override fun wifiDetailsClickListener(wifi: ScanResult) {
        selectedWifi = wifi
        wifiConfiguration = wifiViewModel.getCurrentWifiConfiguration(selectedWifi)
        connectToWifi()
    }

    override fun wifiSelectedClickListener(wifi: ScanResult) {
        selectedWifi = wifi
        wifiConfiguration = wifiViewModel.getCurrentWifiConfiguration(selectedWifi)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                connectToWifi()
                return true
            }
            2 -> {
                forgetWifi()
                return true
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun connectToWifi() {
        val wifiSecurity = WifiInteractor.checkNetworkCapabilities(selectedWifi.capabilities)
        if (wifiSecurity == "OPEN") {
            WifiInteractor.connectWifi(ssid = selectedWifi.SSID, securityMode = wifiSecurity)
        } else if (!WifiInteractor.isNetworkConnected(selectedWifi.SSID)) {
            if (WifiInteractor.isExistingNetwork(selectedWifi.SSID)) {
                existingWifiDialog = ExistingWifiDialog(wifiConfiguration[0])
                showExistingWifiDialog()
            } else {
                dialog = WifiDialog(selectedWifi)
                showDialog()
            }
        } else {
            ToastUtils.displayToast(this, "Already connected to ${selectedWifi.SSID}")
        }
    }

    override fun onResume() {
        super.onResume()
        getConnectionActivity()
        if (hasGps) {
            LocationInteractor.checkLocationStateAndScan(wifiViewModel.getWifi(), resources, this)
        }
    }

    private fun showDialog() {
        dialog.isCancelable = true
        dialog.show(supportFragmentManager, "")
        wifiViewModel.getWifi()
    }

    private fun showExistingWifiDialog() {
        existingWifiDialog.isCancelable = true
        existingWifiDialog.show(supportFragmentManager, "")
        wifiViewModel.getWifi()
    }

    private fun dismissDialog() {
        if (this::dialog.isInitialized) {
            dialog.dismiss()
        }

        if (this::existingWifiDialog.isInitialized) {
            existingWifiDialog.dismiss()
        }
    }

    private fun forgetWifi() {
        WifiInteractor.forgetWifi(wifiConfiguration[0])
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiViewModel.closeDisposable()
        wifiViewModel.unregisterReceiver()
    }
}