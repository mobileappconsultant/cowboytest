package com.example.bluetoothtestapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.bluetoothtestapp.model.AvailableDevice
import com.example.bluetoothtestapp.ui.bluetooth.BluetoothManagerWrapper
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.hamcrest.CoreMatchers.any
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


class BluetoothViewModelTest {

    @MockK
    lateinit var bleManager: BluetoothManagerWrapper

    @InjectMockKs
    lateinit var sut: BluetoothViewModel

    @MockK
    lateinit var bluetoothGatt: BluetoothGatt

    @MockK
    lateinit var bluetoothAdapter: BluetoothAdapter

    @MockK
    lateinit var bluetoothDeviceScanner: BluetoothLeScanner

    private val availableDDevice = AvailableDevice(
        mockk(),
        -42
    )

    @Rule
    @JvmField
    val rule: TestRule = InstantTaskExecutorRule()


    @Before
    fun setUp() = MockKAnnotations.init(this, relaxed = true)


    @Test
    fun verifyDeviceConnected() {

        every { bleManager.bluetoothManager } returns mockk()
        sut.connectToDevice(availableDDevice)

        verify {
            bleManager.connectToDevice(availableDDevice, any())
        }
    }

    @Test
    fun verifyDisconnection() {

        every { bleManager.bluetoothManager } returns mockk()
        sut.disconnect()

        verify {
           bluetoothGatt.disconnect()

        }
    }

    @Test
    fun verifyBluetoothCheck() {

        coEvery { bluetoothAdapter.isEnabled } returns false
        every { bleManager.bluetoothManager } returns mockk()
        val a =sut.isBluetoothEnabled()

        assert(!a)
    }
}