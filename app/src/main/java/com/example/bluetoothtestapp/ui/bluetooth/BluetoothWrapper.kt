package com.example.bluetoothtestapp.ui.bluetooth

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.bluetoothtestapp.model.AvailableDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothManagerWrapper @Inject constructor(
    private val application: Application
) {
    lateinit var bluetoothManager: BluetoothManager
        private set


    init {
        initBle()
    }

    private fun initBle() {
        bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    fun connectToDevice(
        device: AvailableDevice,
        bluetoothConnectionCallBack: BluetoothGattCallback
    ): BluetoothGatt {
        return device.device.connectGatt(application, true, bluetoothConnectionCallBack)
    }
}