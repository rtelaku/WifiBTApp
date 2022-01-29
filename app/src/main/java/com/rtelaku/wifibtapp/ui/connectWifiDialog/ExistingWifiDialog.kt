package com.rtelaku.wifibtapp.ui.connectWifiDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiConfiguration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rtelaku.wifibtapp.R
import com.rtelaku.wifibtapp.databinding.ExistingWifiDialogBinding
import com.rtelaku.wifibtapp.utils.WifiInteractor

class ExistingWifiDialog(val wifi: WifiConfiguration) : DialogFragment() {

    private lateinit var binding: ExistingWifiDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ExistingWifiDialogBinding.inflate(layoutInflater)

        binding.dialogTitle.text = "Do you want to connect to ${wifi.SSID}?"
        initClickListeners()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation

        return binding.root
    }

    private fun initClickListeners() {
        binding.connectButton.setOnClickListener {
            WifiInteractor.connectExistingWifi(wifi)
            dialog?.dismiss()
        }

        binding.forgetButton.setOnClickListener {
            WifiInteractor.forgetWifi(wifi)
            dialog?.dismiss()
        }
    }
}