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
        // Handler for splash transitions and timed event routing
        val handler = Handler(Looper.getMainLooper())
    }

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    fun startTimeout(onNavigate: () -> Unit) {
        // Dispatch splash duration timer to progress main application initialization
        handler.postDelayed({
            _isFinished.value = true
            onNavigate()
        }, 3000)
    }

    override fun onCleared() {
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }
}