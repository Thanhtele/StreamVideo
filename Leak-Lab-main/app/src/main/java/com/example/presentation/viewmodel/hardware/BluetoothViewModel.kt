package com.example.presentation.viewmodel.hardware

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DeviceEntity
import com.example.data.di.ServiceLocator
import com.example.data.repository.DeviceRepository
import com.example.data.repository.DeviceScanListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel : ViewModel(), DeviceScanListener {
    private val deviceRepository = ServiceLocator.deviceRepository

    private val _devices = MutableStateFlow<List<DeviceEntity>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null

    init {
        // Register to the localized hardware scan callback events
        DeviceRepository.scanListeners.add(this)

        viewModelScope.launch {
            deviceRepository?.scannedDevices?.collect {
                _devices.value = it
            }
        }
    }

    fun startScan() {
        _isScanning.value = true

        // Simulating periodic scan results using a Handler loop
        scanRunnable = object : Runnable {
            override fun run() {
                val mac = "00:1A:7D:DA:71:" + (10..99).random()
                val dev = DeviceEntity(
                    macAddress = mac,
                    name = listOf("Galaxy Watch", "Sony XM5", "Logitech Mouse", "Smart TV", "HomePod").random(),
                    rssi = -(50..95).random(),
                    type = "BLE",
                    isPaired = false,
                    lastSeen = System.currentTimeMillis()
                )
                viewModelScope.launch {
                    deviceRepository?.addScannedDevice(dev)
                }

                // Scan loop runs every 3 seconds
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(scanRunnable!!)
    }

    fun stopScan() {
        _isScanning.value = false
        scanRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDeviceScanned(device: DeviceEntity) {
        // Handle direct callback event
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up loop parameters on clearance
    }
}