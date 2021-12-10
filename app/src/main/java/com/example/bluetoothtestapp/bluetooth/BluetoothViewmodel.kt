package com.example.bluetoothtestapp.bluetooth

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bluetoothtestapp.model.AvailableDevice
import dagger.hilt.android.qualifiers.ApplicationContext

class BluetoothViewmodel @ViewModelInject constructor(
    @ApplicationContext applicationContext:Context,
    ): ViewModel() {

    private val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private val bluetoothDeviceScanner = bluetoothAdapter.bluetoothLeScanner
    val listOfDevices = ArrayList<AvailableDevice>()

    private val _bluetoothDevicesFound = MutableLiveData<ArrayList<AvailableDevice>>()
    val bluetoothDevicesFound
        get() = _bluetoothDevicesFound

    private val mainHandler = Handler(
        Looper.getMainLooper()
    )

    private val resumeScan = object : Runnable {
        override fun run() {
            updateDeviceList()

            startScanForDevices()
            mainHandler.postDelayed(this, 10000L)
        }
    }

    fun updateDeviceList() {
        listOfDevices.sortByDescending { it.signalStrength }
        bluetoothDevicesFound.value = listOfDevices.map { it.copy() } as ArrayList<AvailableDevice>
    }

    fun scanForAvailableDevices() {
        startScanForDevices()
        mainHandler.removeCallbacks(resumeScan)
        mainHandler.post(resumeScan)
    }

    fun startScanForDevices() {
        bluetoothDeviceScanner?.stopScan(scanCallBack)

        val scanBuilder = ScanSettings.Builder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanBuilder
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        }
        val scanSettings = scanBuilder
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0L)
            .build()
        bluetoothDeviceScanner.startScan(null, scanSettings, scanCallBack)
    }

    private fun stopScanning() {
        bluetoothDeviceScanner?.stopScan(scanCallBack)
        mainHandler.removeCallbacks(resumeScan)
    }

    private val scanCallBack: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            handleResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (scanResult in results) {
                handleResult(scanResult)
            }
        }

        private fun handleResult(scanResult: ScanResult) {
            listOfDevices.forEach { device ->
                if (device.device.address == scanResult.device.address) {
                    device.signalStrength = scanResult.rssi
                    return
                }
            }
            addAvailableDevice(listOfDevices, scanResult)
        }

        private fun addAvailableDevice(deviceList: ArrayList<AvailableDevice>, result: ScanResult) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceList.add(AvailableDevice(result.device, result.rssi, result.isConnectable))
            } else {
                deviceList.add(AvailableDevice(result.device, result.rssi))
            }
        }
    }

    fun connectToDevice(availableDevice: AvailableDevice) {

    }

    fun isBluetoothEnabled(): Boolean {
        return (bluetoothAdapter!= null && bluetoothAdapter.isEnabled)
    }

}