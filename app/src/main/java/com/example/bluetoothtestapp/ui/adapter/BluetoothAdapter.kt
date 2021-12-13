package com.example.bluetoothtestapp.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.databinding.LayoutDeviceItemBinding
import com.example.bluetoothtestapp.model.AvailableDevice
import com.example.bluetoothtestapp.ui.extensions.makeGone
import com.example.bluetoothtestapp.ui.extensions.makeVisible

class BluetoothAdapter(private val context: Context, val onClick: (AvailableDevice) -> Unit) : RecyclerView.Adapter<BluetoothAdapter.BluetoothDeviceViewHolder>() {

    private var listOfDevices = emptyList<AvailableDevice>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder(LayoutDeviceItemBinding.bind(LayoutInflater.from(context).inflate(R.layout.layout_device_item, parent, false)))
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        holder.bind(listOfDevices[position])
    }

    override fun getItemCount(): Int = listOfDevices.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(availableDevice:  List<AvailableDevice>) {
        this.listOfDevices = availableDevice
        notifyDataSetChanged()
    }

    inner class BluetoothDeviceViewHolder(val binding: LayoutDeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(element: AvailableDevice) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                binding.deviceName.text = element.device.alias ?: element.device.address
            } else {
                binding.deviceName.text = element.device.name ?: element.device.address
            }
            binding.strength.text = element.signalStrength.toString()
            if (element.isDeviceConnectable) binding.button.makeVisible()
            else binding.button.makeGone()
            binding.button.setOnClickListener {
                onClick.invoke(element)
            }
        }
    }
}
