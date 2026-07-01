package com.example.presentation.viewmodel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.NotificationEntity
import com.example.data.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val notificationRepository = ServiceLocator.notificationRepository

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            notificationRepository?.allNotifications?.collect {
                _notifications.value = it
            }
        }
    }

    fun createNotification(title: String, body: String) {
        viewModelScope.launch {
            notificationRepository?.addNotification(title, body, "DIAGNOSTICS")
        }
    }
}