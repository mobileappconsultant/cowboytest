package com.example.bluetoothtestapp.bluetooth

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bluetoothtestapp.model.AvailableDevice
import com.example.bluetoothtestapp.utils.Resources
import dagger.hilt.android.qualifiers.ApplicationContext

class BluetoothViewModel @ViewModelInject constructor(
    @ApplicationContext val applicationContext: Context,
    ): ViewModel() {

    private val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter = bluetoothManager.adapter

    private var bluetoothGatt: BluetoothGatt? = null

    private val bluetoothDeviceScanner = bluetoothAdapter.bluetoothLeScanner
    val listOfDevices = ArrayList<AvailableDevice>()

    private val _bluetoothDevicesFound = MutableLiveData<Resources<ArrayList<AvailableDevice>>>()
    val bluetoothDevicesFound
        get() = _bluetoothDevicesFound

    private val mainHandler = Handler(
        Looper.getMainLooper()
    )

    private val _connectionState: MutableLiveData<ConnectionStatus> = MutableLiveData()
    val connectionState:LiveData<ConnectionStatus> = _connectionState

    private val _connectedDevice = MutableLiveData<AvailableDevice?>()
    val connectedDevice:LiveData<AvailableDevice?> = _connectedDevice

    private var targetDevice: AvailableDevice? = null

    private val _servicesAndCharacteristics =
        MutableLiveData<HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>>>()
    val servicesAndCharacteristics
        get() = _servicesAndCharacteristics


    private val resumeScan = object : Runnable {
        override fun run() {
            updateDeviceList()

            startScanForDevices()
            mainHandler.postDelayed(this, 10000L)
        }
    }

    fun updateDeviceList() {
        listOfDevices.sortByDescending { it.signalStrength }
        bluetoothDevicesFound.value = Resources.Success(listOfDevices.map { it.copy() } as ArrayList<AvailableDevice>)
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

    fun isBluetoothEnabled(): Boolean {
        return (bluetoothAdapter!= null && bluetoothAdapter.isEnabled)
    }

    private val bluetoothConnectionCallBack: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt?.discoverServices()
                _connectionState.postValue(ConnectionStatus.CONNECTED)

                targetDevice?.let {
                    _connectedDevice.postValue(it)
                }
            } else {
                _connectionState.postValue(ConnectionStatus.DISCONNECTED)
                _connectedDevice.postValue(null)
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

    fun connectToDevice(it: AvailableDevice) {
        targetDevice = it

        stopScanning()

        bluetoothGatt = it.device.connectGatt(applicationContext, true, bluetoothConnectionCallBack)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }
}