package com.example.bluetoothtestapp.ui.adapter

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothtestapp.databinding.ItemDeviceDetailBinding
import com.example.bluetoothtestapp.databinding.ItemServiceDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothDetailsAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var infoItemList = ArrayList<InfoItem>()

    fun mapServiceToCharacteristics(map: HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>) {
        adapterScope.launch {
            val listOfInfoItem = ArrayList<InfoItem>()
            if (map.values.isNotEmpty()) {
                map.keys.forEach { service ->
                    listOfInfoItem.add(InfoItem.Service(service))

                    map[service]?.let { list ->
                        listOfInfoItem.addAll(
                            list.map { characteristic ->
                                InfoItem.Characteristic(characteristic)
                            }
                        )
                    }
                }
            }
            infoItemList = listOfInfoItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SERVICE_VIEW_TYPE -> ServiceViewHolder.from(parent)
            CHARACTERISTICS_VIEW_TYPE -> CharacteristicViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ServiceViewHolder -> {
                val item = infoItemList[position] as InfoItem.Service
                holder.bind(item.service)
            }
            is CharacteristicViewHolder -> {
                val item = infoItemList[position] as InfoItem.Characteristic
                holder.bind(item.characteristic)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (infoItemList[position]) {
            is InfoItem.Service -> SERVICE_VIEW_TYPE
            is InfoItem.Characteristic -> CHARACTERISTICS_VIEW_TYPE
        }
    }

    class ServiceViewHolder private constructor(val binding: ItemServiceDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BluetoothGattService) {
            binding.deviceInstance.text = "Service Id - ".plus(item.instanceId.toString())
            binding.deviceUuid.text = item.uuid.toString()

        }

        companion object {
            fun from(parent: ViewGroup): ServiceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemServiceDetailBinding.inflate(layoutInflater, parent, false)

                return ServiceViewHolder(binding)
            }
        }
    }

    class CharacteristicViewHolder private constructor(val binding: ItemDeviceDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BluetoothGattCharacteristic) {
            binding.deviceInstance.text = item.instanceId.toString()
            binding.deviceUuid.text = item.uuid.toString()
        }

        companion object {
            fun from(parent: ViewGroup): CharacteristicViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeviceDetailBinding.inflate(layoutInflater, parent, false)

                return CharacteristicViewHolder(binding)
            }
        }
    }

    sealed class InfoItem {
        data class Service(val service: BluetoothGattService) : InfoItem() {
            override val id = service.instanceId
        }

        data class Characteristic(val characteristic: BluetoothGattCharacteristic) : InfoItem() {
            override val id: Int = characteristic.instanceId
        }

        abstract val id: Int
    }

    override fun getItemCount() = infoItemList.size

    companion object {
        private const val SERVICE_VIEW_TYPE = 0
        private const val CHARACTERISTICS_VIEW_TYPE = 1
    }
}