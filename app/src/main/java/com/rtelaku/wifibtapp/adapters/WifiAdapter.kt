package com.rtelaku.wifibtapp.adapters

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rtelaku.wifibtapp.R
import com.rtelaku.wifibtapp.databinding.WifiItemBinding
import com.rtelaku.wifibtapp.services.OnClick
import com.rtelaku.wifibtapp.utils.WifiInteractor

class WifiAdapter(var wifiItemClickListener: OnClick) :  RecyclerView.Adapter<WifiAdapter.WifiViewHolder>() {

    private var wifiList: ArrayList<ScanResult> = ArrayList()
    private var currentlyConnectedWifiSSID = ""

    inner class WifiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var binding: WifiItemBinding = WifiItemBinding.bind(itemView)
        private var wifiName : String? = null
        fun bindViewWifiData(wifi: ScanResult) {
            wifiName = wifi.SSID

            if(wifiName.equals(currentlyConnectedWifiSSID)) {
                binding.connectionState.text = "Connected"
            } else {
                binding.connectionState.text = ""
            }
            binding.recycleWifiItem.text = wifiName
            setWifiImageLevel(wifi.level)
        }

        private fun setWifiImageLevel(wifiLevel : Int) {
            binding.wifiIcon.setImageLevel(if(wifiLevel >= -60){
                3
            } else if (wifiLevel < -60 && wifiLevel >= -67) {
                2
            } else if (wifiLevel < -67 && wifiLevel >= -89) {
                1
            } else {
                0
            })
        }

        init {
            itemView.setOnClickListener {
                val bindingAdapterPosition = bindingAdapterPosition
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val selectedWifi = wifiList[bindingAdapterPosition]
                    wifiItemClickListener.wifiDetailsClickListener(selectedWifi)
                }
            }

            itemView.setOnCreateContextMenuListener { menu, _, _ ->
                val bindingAdapterPosition = bindingAdapterPosition
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val selectedWifi = wifiList[bindingAdapterPosition]
                    wifiItemClickListener.wifiSelectedClickListener(selectedWifi)
                }

                menu?.setHeaderTitle(wifiName)
                if(!WifiInteractor.isNetworkConnected(wifiName)) {
                    menu?.add(this.bindingAdapterPosition, 1, 1, "Connect to this network")
                } else {
                    menu?.add(this.bindingAdapterPosition, 2, 1, "Forget")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wifi_item, parent, false)
        return WifiViewHolder(view)
    }

    override fun onBindViewHolder(holder: WifiViewHolder, position: Int) {
        val wifiItem = wifiList[position]
        holder.bindViewWifiData(wifiItem)
    }

    override fun getItemCount(): Int {
        return wifiList.size
    }

    fun addWifiList(wifiList: ArrayList<ScanResult>){
        this.wifiList = wifiList
        notifyDataSetChanged()
    }

    fun updateConnectedWifi(newSSID: String) {
        val currentIndex = wifiList.indexOfFirst { it.SSID == currentlyConnectedWifiSSID }
        val newIndex = wifiList.indexOfFirst { it.SSID == newSSID }
        currentlyConnectedWifiSSID = newSSID
        notifyItemChanged(currentIndex)
        notifyItemChanged(newIndex)
    }

    fun clearList(){
        this.wifiList.clear()
        notifyDataSetChanged()
    }
}


