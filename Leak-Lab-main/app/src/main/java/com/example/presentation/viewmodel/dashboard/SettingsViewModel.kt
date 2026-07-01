package com.example.presentation.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _themeState = MutableStateFlow(true) // true = dark, false = light
    val themeState = _themeState.asStateFlow()

    fun toggleTheme() {
        _themeState.value = !_themeState.value
    }
}