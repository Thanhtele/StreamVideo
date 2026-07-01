package com.example.presentation.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {
    private val _sensorValues = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val sensorValues = _sensorValues.asStateFlow()

    fun updateSensor(x: Float, y: Float, z: Float) {
        _sensorValues.value = floatArrayOf(x, y, z)
    }
}