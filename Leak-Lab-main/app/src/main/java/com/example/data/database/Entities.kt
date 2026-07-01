package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String,
    val isPremium: Boolean,
    val loginTimestamp: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val messageText: String,
    val timestamp: Long,
    val isSentByMe: Boolean
)

@Entity(tableName = "scanned_devices")
data class DeviceEntity(
    @PrimaryKey val macAddress: String,
    val name: String,
    val rssi: Int,
    val type: String,
    val isPaired: Boolean,
    val lastSeen: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val url: String,
    val progress: Int,
    val status: String, // PENDING, DOWNLOADING, COMPLETED, FAILED
    val totalSize: Long,
    val timestamp: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val category: String,
    val timestamp: Long,
    val isRead: Boolean
)

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val status: String,
    val durationMs: Long,
    val timestamp: Long,
    val details: String
)
