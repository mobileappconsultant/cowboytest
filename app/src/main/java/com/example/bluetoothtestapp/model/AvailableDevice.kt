package com.example.bluetoothtestapp.model

import android.bluetooth.BluetoothDevice

data class AvailableDevice(
    val device: BluetoothDevice,
    var signalStrength: Int,
    val isDeviceConnectable: Boolean = true
)