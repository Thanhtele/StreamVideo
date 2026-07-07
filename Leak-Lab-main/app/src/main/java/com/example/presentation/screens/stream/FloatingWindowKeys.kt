package com.example.presentation.screens.stream

import androidx.datastore.preferences.core.intPreferencesKey

object FloatingWindowKeys {
    val X = intPreferencesKey("x")
    val Y = intPreferencesKey("y")
    val WIDTH = intPreferencesKey("width")
    val HEIGHT = intPreferencesKey("height")
}