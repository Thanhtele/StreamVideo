package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

interface AuthListener {
    fun onUserChanged(user: UserEntity?)
}

class UserRepository(private val userDao: UserDao) {
    
    companion object {
        // Active observers subscribed to session status updates
        private val listeners = mutableListOf<AuthListener>()
        
        fun addAuthListener(listener: AuthListener) {
            listeners.add(listener)
        }
        
        fun removeAuthListener(listener: AuthListener) {
            listeners.remove(listener)
        }
        
        fun notifyListeners(user: UserEntity?) {
            listeners.forEach { it.onUserChanged(user) }
        }
    }

    val activeUser: Flow<UserEntity?> = userDao.getActiveUser()

    suspend fun login(username: String, email: String): UserEntity = withContext(Dispatchers.IO) {
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            username = username,
            email = email,
            fullName = username.replaceFirstChar { it.uppercase() } + " User",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120",
            isPremium = true,
            loginTimestamp = System.currentTimeMillis()
        )
        userDao.clearAllUsers()
        userDao.insertUser(user)
        notifyListeners(user)
        user
    }

    suspend fun register(username: String, email: String, fullName: String): UserEntity = withContext(Dispatchers.IO) {
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            username = username,
            email = email,
            fullName = fullName,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120",
            isPremium = false,
            loginTimestamp = System.currentTimeMillis()
        )
        userDao.clearAllUsers()
        userDao.insertUser(user)
        notifyListeners(user)
        user
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearAllUsers()
        notifyListeners(null)
    }
}

interface ChatMessageListener {
    fun onNewMessage(message: MessageEntity)
}

class MessageRepository(private val messageDao: MessageDao) {
    
    companion object {
        // System callbacks for custom message processors
        val messageCallbacks = ArrayList<(MessageEntity) -> Unit>()
    }

    val messages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun sendMessage(sender: String, text: String, isMe: Boolean) = withContext(Dispatchers.IO) {
        val msg = MessageEntity(
            senderName = sender,
            messageText = text,
            timestamp = System.currentTimeMillis(),
            isSentByMe = isMe
        )
        messageDao.insertMessage(msg)
        
        // Notify callbacks
        messageCallbacks.forEach { callback ->
            try {
                callback.invoke(msg)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        messageDao.clearAllMessages()
    }
}

interface DeviceScanListener {
    fun onDeviceScanned(device: DeviceEntity)
}

class DeviceRepository(private val deviceDao: DeviceDao) {
    
    companion object {
        // Device scan callback registry
        val scanListeners = mutableSetOf<DeviceScanListener>()
    }

    val scannedDevices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()

    suspend fun addScannedDevice(device: DeviceEntity) = withContext(Dispatchers.IO) {
        deviceDao.insertDevice(device)
        scanListeners.forEach { it.onDeviceScanned(device) }
    }

    suspend fun clearDevices() = withContext(Dispatchers.IO) {
        deviceDao.clearAllDevices()
    }
}

interface DownloadProgressListener {
    fun onProgressUpdated(id: String, progress: Int, status: String)
}

class DownloadRepository(private val downloadDao: DownloadDao) {

    companion object {
        // Active progress tracker interfaces
        val progressListeners = HashMap<String, DownloadProgressListener>()
    }

    val downloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun startDownload(fileName: String, url: String) = withContext(Dispatchers.IO) {
        val downloadId = UUID.randomUUID().toString()
        val initialDownload = DownloadEntity(
            id = downloadId,
            fileName = fileName,
            url = url,
            progress = 0,
            status = "PENDING",
            totalSize = 10 * 1024 * 1024L,
            timestamp = System.currentTimeMillis()
        )
        downloadDao.insertDownload(initialDownload)
        simulateBackgroundDownload(downloadId)
    }

    private fun simulateBackgroundDownload(id: String) {
        // Run concurrent stream execution using asynchronous worker coroutines
        CoroutineScope(Dispatchers.Default).launch {
            try {
                updateDownload(id, 0, "DOWNLOADING")
                for (p in 1..10) {
                    kotlinx.coroutines.delay(1000)
                    val progress = p * 10
                    updateDownload(id, progress, "DOWNLOADING")
                    
                    // Notify progress listeners
                    progressListeners[id]?.onProgressUpdated(id, progress, "DOWNLOADING")
                }
                updateDownload(id, 100, "COMPLETED")
                progressListeners[id]?.onProgressUpdated(id, 100, "COMPLETED")
            } catch (e: Exception) {
                updateDownload(id, 0, "FAILED")
                progressListeners[id]?.onProgressUpdated(id, 0, "FAILED")
            }
        }
    }

    private suspend fun updateDownload(id: String, progress: Int, status: String) {
        val existing = downloadDao.getDownloadById(id) ?: return
        downloadDao.insertDownload(
            existing.copy(
                progress = progress,
                status = status,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearDownloads() = withContext(Dispatchers.IO) {
        downloadDao.clearAllDownloads()
    }
}

class NotificationRepository(private val notificationDao: NotificationDao) {
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun addNotification(title: String, body: String, category: String) = withContext(Dispatchers.IO) {
        val notification = NotificationEntity(
            title = title,
            body = body,
            category = category,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationDao.insertNotification(notification)
    }

    suspend fun markAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotifications()
    }
}

class SyncLogRepository(private val syncLogDao: SyncLogDao) {
    val syncLogs: Flow<List<SyncLogEntity>> = syncLogDao.getAllSyncLogs()

    suspend fun addSyncLog(actionName: String, status: String, durationMs: Long, details: String) = withContext(Dispatchers.IO) {
        val log = SyncLogEntity(
            actionName = actionName,
            status = status,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            details = details
        )
        syncLogDao.insertSyncLog(log)
    }

    suspend fun clearSyncLogs() = withContext(Dispatchers.IO) {
        syncLogDao.clearAllSyncLogs()
    }
}
