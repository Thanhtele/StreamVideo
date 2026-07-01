package com.example.presentation.viewmodel.task

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Timer
import java.util.TimerTask

class StatisticsViewModel : ViewModel() {
    private val _cpuLoad = MutableStateFlow(0f)
    val cpuLoad = _cpuLoad.asStateFlow()

    private val _memoryUsage = MutableStateFlow(0f)
    val memoryUsage = _memoryUsage.asStateFlow()

    private var timer: Timer? = null

    fun startPolling() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Polling CPU and Memory load
                _cpuLoad.value = (30..85).random().toFloat()
                _memoryUsage.value = (150..450).random().toFloat()
            }
        }, 0, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        // Release active worker scheduling systems
        timer?.cancel()
        timer = null
    }
}