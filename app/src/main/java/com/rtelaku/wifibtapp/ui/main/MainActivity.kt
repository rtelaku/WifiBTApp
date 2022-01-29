package com.rtelaku.wifibtapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rtelaku.wifibtapp.databinding.ActivityMainBinding
import com.rtelaku.wifibtapp.services.OnClick
import com.rtelaku.wifibtapp.ui.bluetooth.BluetoothActivity
import com.rtelaku.wifibtapp.ui.wifi.WifiActivity

class MainActivity : AppCompatActivity(), OnClick {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        binding.wifiCard.setOnClickListener {
            wifiOnClickListener()
        }

        binding.bluetoothCard.setOnClickListener {
            bluetoothOnClickListener()
        }
    }

    override fun wifiOnClickListener() {
        val intent = Intent(this@MainActivity, WifiActivity::class.java)
        startActivity(intent)
    }

    override fun bluetoothOnClickListener() {
        val intent = Intent(this@MainActivity, BluetoothActivity::class.java)
        startActivity(intent)
    }
}