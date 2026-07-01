package com.example.presentation.viewmodel.task

import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SyncLogEntity
import com.example.data.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackgroundSyncViewModel : ViewModel() {
    private val syncRepository = ServiceLocator.syncLogRepository

    private val _syncLogs = MutableStateFlow<List<SyncLogEntity>>(emptyList())
    val syncLogs = _syncLogs.asStateFlow()

    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    init {
        viewModelScope.launch {
            syncRepository?.syncLogs?.collect {
                _syncLogs.value = it
            }
        }

        // Spin up background processor thread to manage offline synchronization logs
        handlerThread = HandlerThread("DiagnosticsSyncThread")
        handlerThread?.start()
        backgroundHandler = Handler(handlerThread!!.looper)
    }

    fun triggerLocalSync() {
        backgroundHandler?.post {
            // Simulated processing inside background thread
            viewModelScope.launch {
                syncRepository?.addSyncLog(
                    actionName = "Manual Local Sync",
                    status = "SUCCESS",
                    durationMs = 450L,
                    details = "Processed manual local memory diagnostics check."
                )
            }
        }
    }
}