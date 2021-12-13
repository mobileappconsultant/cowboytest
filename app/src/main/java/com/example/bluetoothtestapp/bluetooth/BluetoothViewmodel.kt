package com.example.bluetoothtestapp.bluetooth

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothtestapp.model.AvailableDevice
import com.example.bluetoothtestapp.ui.bluetooth.BluetoothManagerWrapper
import com.example.bluetoothtestapp.utils.Resources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bleManager: BluetoothManagerWrapper,
): ViewModel() {

    private val bluetoothAdapter = bleManager.bluetoothManager.adapter

    private var bluetoothGatt: BluetoothGatt? = null

    private val bluetoothDeviceScanner = bluetoothAdapter.bluetoothLeScanner
    val listOfDevices = ArrayList<AvailableDevice>()

    private val _bluetoothDevicesFound = MutableLiveData<Resources<ArrayList<AvailableDevice>>>()
    val bluetoothDevicesFound: LiveData<Resources<ArrayList<AvailableDevice>>> =
        _bluetoothDevicesFound

    fun startUpdates() {
        viewModelScope.launch {
            while(true) {
                updateDeviceList()

                startScanForDevices()
                delay(10000)
            }
        }
    }

    private val _connectionState: MutableLiveData<ConnectionItem> = MutableLiveData()
    val connectionState: LiveData<ConnectionItem> = _connectionState

    private var targetDevice: AvailableDevice? = null

    private val _servicesAndCharacteristics =
        MutableLiveData<HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>>()
    val servicesAndCharacteristics
        get() = _servicesAndCharacteristics

    fun updateDeviceList() {
        listOfDevices.sortByDescending { it.signalStrength }
        _bluetoothDevicesFound.value =
            Resources.Success(listOfDevices.map { it.copy() } as ArrayList<AvailableDevice>)
    }

    fun scanForAvailableDevices() {
        startUpdates()
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

    fun isBluetoothEnabled(): Boolean {
        return (bluetoothAdapter!= null && bluetoothAdapter.isEnabled)
    }

    private val bluetoothConnectionCallBack: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt?.discoverServices()

                _connectionState.postValue(
                    ConnectionItem(
                        ConnectionStatus.CONNECTED,
                        targetDevice?.device?.name
                    )
                )

            } else {
                _connectionState.postValue(ConnectionItem(ConnectionStatus.DISCONNECTED))
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val res = HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>()

                bluetoothGatt?.services?.forEach { service ->
                    val characteristicList = ArrayList<BluetoothGattCharacteristic>()
                    service.characteristics.forEach { characteristic ->
                        characteristicList.add(characteristic)
                    }
                    res[service] = characteristicList
                }
                _servicesAndCharacteristics.postValue(res)
            }
        }
    }

    fun connectToDevice(device: AvailableDevice) {
        targetDevice = device
//        stopScanning()
        bluetoothGatt = bleManager.connectToDevice(device, bluetoothConnectionCallBack)

    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    data class ConnectionItem(
        val connectionStatus: ConnectionStatus,
        val deviceName: String? = ""
    )
}