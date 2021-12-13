package com.example.bluetoothtestapp.bluetooth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.bluetoothtestapp.model.AvailableDevice
import com.example.bluetoothtestapp.ui.bluetooth.BluetoothManagerWrapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


class BluetoothViewModelTest {

    @MockK
    lateinit var bleManager: BluetoothManagerWrapper

    @InjectMockKs
    lateinit var sut: BluetoothViewModel

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
}