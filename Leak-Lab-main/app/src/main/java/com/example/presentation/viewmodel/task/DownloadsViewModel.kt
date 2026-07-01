package com.example.presentation.viewmodel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DownloadEntity
import com.example.data.di.ServiceLocator
import com.example.data.repository.DownloadProgressListener
import com.example.data.repository.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadsViewModel : ViewModel(), DownloadProgressListener {
    private val downloadRepository = ServiceLocator.downloadRepository

    private val _downloadsState = MutableStateFlow<List<DownloadEntity>>(emptyList())
    val downloadsState = _downloadsState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadRepository?.downloads?.collect {
                _downloadsState.value = it
            }
        }
    }

    fun startNewDownload(fileName: String, url: String) {
        viewModelScope.launch {
            // Store listener reference for status tracking callbacks
            DownloadRepository.progressListeners[fileName] = this@DownloadsViewModel
            downloadRepository?.startDownload(fileName, url)
        }
    }

    override fun onProgressUpdated(id: String, progress: Int, status: String) {
        // Notify progress
    }
}