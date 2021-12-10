package com.example.bluetoothtestapp.ui.home


import android.Manifest
import android.content.pm.PackageManager
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothtestapp.bluetooth.BluetoothViewmodel
import com.example.bluetoothtestapp.databinding.FragmentHomeBinding
import com.example.bluetoothtestapp.ui.adapter.BluetoothAdapter
import com.example.bluetoothtestapp.ui.base.BaseFragment
import com.example.bluetoothtestapp.ui.extensions.makeGone
import com.example.bluetoothtestapp.ui.extensions.makeVisible
import com.example.bluetoothtestapp.ui.extensions.toastMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Home: BaseFragment<FragmentHomeBinding>() {

    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun getViewBinding(): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    private val viewModel: BluetoothViewmodel by activityViewModels()

    override fun setUp() {

        binding.deviceList.layoutManager = LinearLayoutManager(requireContext())

        bluetoothAdapter = BluetoothAdapter(requireContext()) {
            val action = HomeDirections.actionHomeFragmentToDeviceDetailFragment()
            launchFragment(action)
        }
        binding.deviceList.adapter = bluetoothAdapter
    }


    override fun observeData() {
        viewModel.bluetoothDevicesFound.observe(viewLifecycleOwner
        ) {
            if(it.isNotEmpty()) {
                binding.deviceList.makeVisible()
                binding.progressBar.makeGone()
                bluetoothAdapter.update(it)
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
                } else {
                    requireContext().toastMessage("Error accessing bluetooth")
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}