package com.example.presentation.viewmodel.auth

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashViewModel : ViewModel() {
    companion object {
        // Cached context for resource loading and system utilities
        var leakContext: Context? = null

        // Handler for splash transitions and timed event routing
        val handler = Handler(Looper.getMainLooper())
    }

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    fun startTimeout(context: Context, onNavigate: () -> Unit) {
        leakContext = context

        // Dispatch splash duration timer to progress main application initialization
        handler.postDelayed(object : Runnable {
            override fun run() {
                _isFinished.value = true
                onNavigate()
            }
        }, 3000)
    }
}