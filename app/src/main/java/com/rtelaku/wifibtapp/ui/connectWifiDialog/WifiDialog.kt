package com.rtelaku.wifibtapp.ui.connectWifiDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rtelaku.wifibtapp.R
import com.rtelaku.wifibtapp.databinding.WifiDialogBinding
import com.rtelaku.wifibtapp.utils.ToastUtils
import com.rtelaku.wifibtapp.utils.WifiBTApplication
import com.rtelaku.wifibtapp.utils.WifiInteractor

class WifiDialog(val wifi: ScanResult) : DialogFragment() {

    private lateinit var binding: WifiDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = WifiDialogBinding.inflate(layoutInflater)
        binding.dialogTitle.text = "Connect to ${wifi.SSID}"

        initClickListeners()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation

        return binding.root
    }

    private fun initClickListeners() {
        binding.submitButton.setOnClickListener {
            val wifiSecurity = WifiInteractor.checkNetworkCapabilities(wifi.capabilities)
            if (getWifiPassword().length < 8) {
                ToastUtils.displayToast(WifiBTApplication.getInstance(), "Invalid password")
            } else {
                WifiInteractor.connectWifi(wifi.SSID, getWifiPassword(), wifiSecurity)
                dialog?.dismiss()
            }
        }
    }

    private fun getWifiPassword(): String {
        return "${binding.wifiPassword.text}"
    }
}