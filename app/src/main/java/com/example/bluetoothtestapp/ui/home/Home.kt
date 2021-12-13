package com.example.bluetoothtestapp.ui.home


import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothtestapp.bluetooth.BluetoothViewModel
import com.example.bluetoothtestapp.bluetooth.ConnectionStatus
import com.example.bluetoothtestapp.databinding.FragmentHomeBinding
import com.example.bluetoothtestapp.ui.adapter.BluetoothAdapter
import com.example.bluetoothtestapp.ui.base.BaseFragment
import com.example.bluetoothtestapp.ui.extensions.makeGone
import com.example.bluetoothtestapp.ui.extensions.makeVisible
import com.example.bluetoothtestapp.ui.extensions.toastMessage
import com.example.bluetoothtestapp.utils.Resources
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Home: BaseFragment<FragmentHomeBinding>() {

    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun getViewBinding(): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    private val viewModel: BluetoothViewModel by activityViewModels()

    override fun setUp() {

        binding.deviceList.layoutManager = LinearLayoutManager(requireContext())

        bluetoothAdapter = BluetoothAdapter(requireContext()) {
            viewModel.connectToDevice(it)
        }
        binding.deviceList.adapter = bluetoothAdapter

        binding.button.setOnClickListener {
            if (viewModel.isBluetoothEnabled()) {
                viewModel.scanForAvailableDevices()
                binding.button.makeGone()
            }
        }
    }

    override fun observeData() {
        viewModel.connectionState.observe(viewLifecycleOwner) {
            when (it) {
                ConnectionStatus.CONNECTED -> {
                    val action = HomeDirections.actionHomeFragmentToDeviceDetailFragment()
                    launchFragment(action)
                }
                else -> Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
            }

        }

        viewModel.bluetoothDevicesFound.observe(viewLifecycleOwner
        ) {
            when(it) {
                is Resources.Success -> {
                    if(it.data.isNotEmpty()) {
                        binding.deviceList.makeVisible()
                        binding.progressBar.makeGone()
                        bluetoothAdapter.update(it.data)
                    }
                }
                is Resources.Failure -> TODO()
                is Resources.Loading -> TODO()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        requestPermissions(permissions,1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (viewModel.isBluetoothEnabled()) {
                    viewModel.scanForAvailableDevices()
                    binding.button.makeGone()
                } else {
                    binding.progressBar.makeGone()
                    requireContext().toastMessage("Error accessing bluetooth")
                    binding.button.makeVisible()
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}