package com.example.bluetoothtestapp.ui.detail

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothtestapp.bluetooth.BluetoothViewModel
import com.example.bluetoothtestapp.databinding.FragmentDeviceDetailBinding
import com.example.bluetoothtestapp.ui.adapter.BluetoothDetailsAdapter
import com.example.bluetoothtestapp.ui.base.BaseFragment

class DeviceDetailFragment : BaseFragment<FragmentDeviceDetailBinding>() {

    override fun getViewBinding(): FragmentDeviceDetailBinding  = FragmentDeviceDetailBinding.inflate(layoutInflater)

    private val viewModel: BluetoothViewModel by activityViewModels()

    private val deviceDetailAdapter = BluetoothDetailsAdapter(requireContext())

    override fun setUp() {

        binding.deviceInfoList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceDetailAdapter
        }
    }

    override fun observeData() {
        viewModel.servicesAndCharacteristics.observe(viewLifecycleOwner, {
            it?.let {
                deviceDetailAdapter.mapServiceToCharacteristics(it)
                deviceDetailAdapter.notifyDataSetChanged()
            }
        })
    }

    fun disconnect() {
        viewModel.disconnect()
    }
}