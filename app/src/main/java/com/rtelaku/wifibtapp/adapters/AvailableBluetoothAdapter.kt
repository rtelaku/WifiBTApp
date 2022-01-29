package com.rtelaku.wifibtapp.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rtelaku.wifibtapp.R
import com.rtelaku.wifibtapp.databinding.BluetoothItemBinding
import com.rtelaku.wifibtapp.services.OnClick

class AvailableBluetoothAdapter(var bluetoothItemClickListener: OnClick) :  RecyclerView.Adapter<AvailableBluetoothAdapter.MyBluetoothAdapter>()  {

    private var bluetoothList:ArrayList<BluetoothDevice> = ArrayList()

    inner class MyBluetoothAdapter(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: BluetoothItemBinding = BluetoothItemBinding.bind(itemView)
        fun bindViewWifiData(bluetooth: BluetoothDevice) {
            binding.recycleBluetoothItem.text = bluetooth.name
        }

        init {
            itemView.setOnClickListener {
                val bindingAdapterPosition = bindingAdapterPosition
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val selectedBluetooth = bluetoothList[bindingAdapterPosition]
                    bluetoothItemClickListener.bluetoothDetailsClickListener(selectedBluetooth)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBluetoothAdapter {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bluetooth_item, parent, false)
        return MyBluetoothAdapter(view)
    }

    override fun onBindViewHolder(holder: MyBluetoothAdapter, position: Int) {
        val wifiItem = bluetoothList[position]
        holder.bindViewWifiData(wifiItem)
    }

    override fun getItemCount(): Int {
        return bluetoothList.size
    }

    fun addBluetoothList(bluetooth: BluetoothDevice){
        this.bluetoothList.add(bluetooth)
        notifyItemInserted(itemCount-1)
    }

    fun clearList(){
        this.bluetoothList.clear()
        notifyDataSetChanged()
    }
}