package com.example.presentation.viewmodel.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DeviceEntity
import com.example.data.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DeviceEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun updateQuery(query: String) {
        _searchQuery.value = query
        // Simulate real filtering
        viewModelScope.launch {
            ServiceLocator.deviceRepository?.scannedDevices?.collect { list ->
                _searchResults.value = list.filter {
                    it.name.contains(query, ignoreCase = true) || it.macAddress.contains(query)
                }
            }
        }
    }
}